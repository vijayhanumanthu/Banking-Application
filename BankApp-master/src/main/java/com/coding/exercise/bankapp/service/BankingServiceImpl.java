package com.coding.exercise.bankapp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    @Override
    public List<CustomerDetails> findAllCustomers() {
        return StreamSupport.stream(customerRepository.findAll().spliterator(), false)
                .map(bankingServiceHelper::convertToCustomerDomain)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDetails updateCustomer(CustomerDetails customerDetails, Long customerNumber) {

        Customer managed = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Customer incoming = bankingServiceHelper.convertToCustomerEntity(customerDetails);

        managed.setFirstName(incoming.getFirstName());
        managed.setMiddleName(incoming.getMiddleName());
        managed.setLastName(incoming.getLastName());
        managed.setStatus(incoming.getStatus());
        managed.setUpdateDateTime(new Date());

        if (incoming.getContactDetails() != null) {
            if (managed.getContactDetails() == null)
                managed.setContactDetails(incoming.getContactDetails());
            else {
                managed.getContactDetails().setEmailId(incoming.getContactDetails().getEmailId());
                managed.getContactDetails().setHomePhone(incoming.getContactDetails().getHomePhone());
                managed.getContactDetails().setWorkPhone(incoming.getContactDetails().getWorkPhone());
            }
        }

        if (incoming.getCustomerAddress() != null) {
            if (managed.getCustomerAddress() == null)
                managed.setCustomerAddress(incoming.getCustomerAddress());
            else {
                managed.getCustomerAddress().setAddress1(incoming.getCustomerAddress().getAddress1());
                managed.getCustomerAddress().setAddress2(incoming.getCustomerAddress().getAddress2());
                managed.getCustomerAddress().setCity(incoming.getCustomerAddress().getCity());
                managed.getCustomerAddress().setState(incoming.getCustomerAddress().getState());
                managed.getCustomerAddress().setZip(incoming.getCustomerAddress().getZip());
                managed.getCustomerAddress().setCountry(incoming.getCustomerAddress().getCountry());
            }
        }

        customerRepository.save(managed);
		return customerDetails;
    }

    @Override
    public void deleteCustomer(Long customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customerRepository.delete(customer);
    }
	
    @Override
    public CustomerDetails findCustomerByNumber(Long customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return bankingServiceHelper.convertToCustomerDomain(customer);
    }

	@Override
	public List<TransactionDetails> findTransactionsByAccountNumber(Long accountNumber) {
		// TODO Auto-generated method stub
		return null;
	}
}
