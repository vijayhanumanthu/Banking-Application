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
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerAccountXRefRepository custAccXRefRepository;
    private final BankingServiceHelper helper;

    public BankingServiceImpl(CustomerRepository customerRepository,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              CustomerAccountXRefRepository custAccXRefRepository,
                              BankingServiceHelper helper) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.custAccXRefRepository = custAccXRefRepository;
        this.helper = helper;
    }
    
    @Override
    public CustomerDetails addCustomer(CustomerDetails customerDetails) {
        Customer customer = helper.convertToCustomerEntity(customerDetails);
        customer.setCreateDateTime(new Date());
        Customer saved = customerRepository.save(customer);
        return helper.convertToCustomerDomain(saved);
    }
	
    @Override
    public AccountInformation findAccountByNumber(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        return helper.convertToAccountDomain(account);
    }

    @Override
    public AccountInformation addNewAccount(AccountInformation accountInformation, Long customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerNumber));

        Account account = helper.convertToAccountEntity(accountInformation);
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

        Transaction debitTxn = helper.createTransaction(
                transferDetails, fromAccount.getAccountNumber(), "DEBIT"
        );

        Transaction creditTxn = helper.createTransaction(
                transferDetails, toAccount.getAccountNumber(), "CREDIT"
        );

        transactionRepository.save(debitTxn);
        transactionRepository.save(creditTxn);
    }

    @Override
    public List<CustomerDetails> findAllCustomers() {
        return StreamSupport.stream(customerRepository.findAll().spliterator(), false)
                .map(helper::convertToCustomerDomain)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDetails updateCustomer(CustomerDetails customerDetails, Long customerNumber) {
        Customer managed = getCustomerOrThrow(customerNumber);
        Customer incoming = helper.convertToCustomerEntity(customerDetails);

        updateBasicDetails(managed, incoming);
        updateContactDetails(managed, incoming);
        updateAddressDetails(managed, incoming);

        Customer updated = customerRepository.save(managed);
        return helper.convertToCustomerDomain(updated);
    }

    @Override
    public void deleteCustomer(Long customerNumber) {
        Customer customer = getCustomerOrThrow(customerNumber);
        customerRepository.delete(customer);
    }
	
    @Override
    public CustomerDetails findCustomerByNumber(Long customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return helper.convertToCustomerDomain(customer);
    }

	@Override
	public List<TransactionDetails> findTransactionsByAccountNumber(Long accountNumber) {
		return null;
	}
}
