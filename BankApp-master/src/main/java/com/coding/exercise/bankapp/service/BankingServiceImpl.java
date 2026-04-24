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
        Account account = getAccountOrThrow(accountNumber);
        return helper.convertToAccountDomain(account);
    }
    
    @Override
    public AccountInformation addNewAccount(AccountInformation accountInformation, Long customerNumber) {
        Customer customer = getCustomerOrThrow(customerNumber);

        Account account = helper.convertToAccountEntity(accountInformation);
        Account savedAccount = accountRepository.save(account);

        custAccXRefRepository.save(CustomerAccountXRef.builder()
                .accountNumber(savedAccount.getAccountNumber())
                .customerNumber(customer.getCustomerNumber())
                .build());

        return helper.convertToAccountDomain(savedAccount);
    }

    @Override
    public void transferAmount(TransferDetails transferDetails, Long customerNumber) {
        getCustomerOrThrow(customerNumber);

        Account fromAccount = getAccountOrThrow(transferDetails.getFromAccountNumber());
        Account toAccount = getAccountOrThrow(transferDetails.getToAccountNumber());

        validateSufficientBalance(fromAccount, transferDetails.getTransferAmount());

        updateBalances(fromAccount, toAccount, transferDetails.getTransferAmount());

        saveTransactions(transferDetails, fromAccount, toAccount);
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
        return helper.convertToCustomerDomain(getCustomerOrThrow(customerNumber));
    }

	@Override
	public List<TransactionDetails> findTransactionsByAccountNumber(Long accountNumber) {
		return null;
	}
}
