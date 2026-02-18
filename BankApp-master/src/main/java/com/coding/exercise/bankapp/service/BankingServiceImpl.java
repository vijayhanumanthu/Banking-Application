package com.coding.exercise.bankapp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.domain.TransferDetails;
import com.coding.exercise.bankapp.model.Account;
import com.coding.exercise.bankapp.model.Address;
import com.coding.exercise.bankapp.model.Contact;
import com.coding.exercise.bankapp.model.Customer;
import com.coding.exercise.bankapp.model.CustomerAccountXRef;
import com.coding.exercise.bankapp.model.Transaction;
import com.coding.exercise.bankapp.repository.AccountRepository;
import com.coding.exercise.bankapp.repository.CustomerAccountXRefRepository;
import com.coding.exercise.bankapp.repository.CustomerRepository;
import com.coding.exercise.bankapp.repository.TransactionRepository;
import com.coding.exercise.bankapp.service.helper.BankingServiceHelper;

@Service
@Transactional
public class BankingServiceImpl implements BankingService {

    private final CustomerRepository customerRepository;
	@Autowired
    private AccountRepository accountRepository;
	@Autowired
    private TransactionRepository transactionRepository;
	@Autowired
    private CustomerAccountXRefRepository custAccXRefRepository;
    @Autowired
    private BankingServiceHelper bankingServiceHelper;

    public BankingServiceImpl(CustomerRepository repository) {
        this.customerRepository=repository;
    }
    
    @Override
    public CustomerDetails addCustomer(CustomerDetails customerDetails) {
        Customer customer = bankingServiceHelper.convertToCustomerEntity(customerDetails);
        customer.setCreateDateTime(new Date());
        customerRepository.save(customer);
		return customerDetails;
    }
	
    @Override
    public AccountInformation findAccountByNumber(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        return bankingServiceHelper.convertToAccountDomain(account);
    }

    @Override
    public AccountInformation addNewAccount(AccountInformation accountInformation, Long customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerNumber));

        Account account = bankingServiceHelper.convertToAccountEntity(accountInformation);
        accountRepository.save(account);

        custAccXRefRepository.save(
                CustomerAccountXRef.builder()
                        .accountNumber(account.getAccountNumber())
                        .customerNumber(customer.getCustomerNumber())
                        .build()
        );
    }

	
    @Override
    public void transferAmount(TransferDetails transferDetails, Long customerNumber) {

        customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Account fromAccount = accountRepository.findByAccountNumber(
                transferDetails.getFromAccountNumber()
        ).orElseThrow(() -> new RuntimeException("From account not found"));

        Account toAccount = accountRepository.findByAccountNumber(
                transferDetails.getToAccountNumber()
        ).orElseThrow(() -> new RuntimeException("To account not found"));

        if (fromAccount.getAccountBalance() < transferDetails.getTransferAmount()) {
            throw new RuntimeException("Insufficient funds");
        }

        // Atomic update via @Transactional
        fromAccount.setAccountBalance(
                fromAccount.getAccountBalance() - transferDetails.getTransferAmount()
        );

        toAccount.setAccountBalance(
                toAccount.getAccountBalance() + transferDetails.getTransferAmount()
        );

        fromAccount.setUpdateDateTime(new Date());
        toAccount.setUpdateDateTime(new Date());

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction debitTxn = bankingServiceHelper.createTransaction(
                transferDetails, fromAccount.getAccountNumber(), "DEBIT"
        );

        Transaction creditTxn = bankingServiceHelper.createTransaction(
                transferDetails, toAccount.getAccountNumber(), "CREDIT"
        );

        transactionRepository.save(debitTxn);
        transactionRepository.save(creditTxn);
    }

	public List<CustomerDetails> findAll() {
    	
    	List<CustomerDetails> allCustomerDetails = new ArrayList<>();

        Iterable<Customer> customerList = customerRepository.findAll();

        customerList.forEach(customer -> {
        	allCustomerDetails.add(bankingServiceHelper.convertToCustomerDomain(customer));
        });
        
        return allCustomerDetails;
    }


	public ResponseEntity<Object> updateCustomer(CustomerDetails customerDetails, Long customerNumber) {
		Optional<Customer> managedCustomerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);
		Customer unmanagedCustomerEntity = bankingServiceHelper.convertToCustomerEntity(customerDetails);
		if(managedCustomerEntityOpt.isPresent()) {
			Customer managedCustomerEntity = managedCustomerEntityOpt.get();
			
			if(Optional.ofNullable(unmanagedCustomerEntity.getContactDetails()).isPresent()) {
				
				Contact managedContact = managedCustomerEntity.getContactDetails();
				if(managedContact != null) {
					managedContact.setEmailId(unmanagedCustomerEntity.getContactDetails().getEmailId());
					managedContact.setHomePhone(unmanagedCustomerEntity.getContactDetails().getHomePhone());
					managedContact.setWorkPhone(unmanagedCustomerEntity.getContactDetails().getWorkPhone());
				} else
					managedCustomerEntity.setContactDetails(unmanagedCustomerEntity.getContactDetails());
			}
			
			if(Optional.ofNullable(unmanagedCustomerEntity.getCustomerAddress()).isPresent()) {
				
				Address managedAddress = managedCustomerEntity.getCustomerAddress();
				if(managedAddress != null) {
					managedAddress.setAddress1(unmanagedCustomerEntity.getCustomerAddress().getAddress1());
					managedAddress.setAddress2(unmanagedCustomerEntity.getCustomerAddress().getAddress2());
					managedAddress.setCity(unmanagedCustomerEntity.getCustomerAddress().getCity());
					managedAddress.setState(unmanagedCustomerEntity.getCustomerAddress().getState());
					managedAddress.setZip(unmanagedCustomerEntity.getCustomerAddress().getZip());
					managedAddress.setCountry(unmanagedCustomerEntity.getCustomerAddress().getCountry());
				} else
					managedCustomerEntity.setCustomerAddress(unmanagedCustomerEntity.getCustomerAddress());
			}
			
			managedCustomerEntity.setUpdateDateTime(new Date());
			managedCustomerEntity.setStatus(unmanagedCustomerEntity.getStatus());
			managedCustomerEntity.setFirstName(unmanagedCustomerEntity.getFirstName());
			managedCustomerEntity.setMiddleName(unmanagedCustomerEntity.getMiddleName());
			managedCustomerEntity.setLastName(unmanagedCustomerEntity.getLastName());
			managedCustomerEntity.setUpdateDateTime(new Date());
			
			customerRepository.save(managedCustomerEntity);
			
			return ResponseEntity.status(HttpStatus.OK).body("Success: Customer updated.");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer Number " + customerNumber + " not found.");
		}
	}

	public ResponseEntity<Object> deleteCustomer(Long customerNumber) {
		
		Optional<Customer> managedCustomerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);

		if(managedCustomerEntityOpt.isPresent()) {
			Customer managedCustomerEntity = managedCustomerEntityOpt.get();
			customerRepository.delete(managedCustomerEntity);
			return ResponseEntity.status(HttpStatus.OK).body("Success: Customer deleted.");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Customer does not exist.");
		}
		
	}
	
	public CustomerDetails findByCustomerNumber(Long customerNumber) {
		
		Optional<Customer> customerEntityOpt = customerRepository.findByCustomerNumber(customerNumber);

		if(customerEntityOpt.isPresent())
			return bankingServiceHelper.convertToCustomerDomain(customerEntityOpt.get());
		
		return null;
	}
}
