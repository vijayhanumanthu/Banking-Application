package com.coding.exercise.bankapp.service;

import java.util.List;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.domain.TransferDetails;

public interface BankingService {

    List<CustomerDetails> findAllCustomers();

    CustomerDetails addCustomer(CustomerDetails customerDetails);

    CustomerDetails findCustomerByNumber(Long customerNumber);

    CustomerDetails updateCustomer(CustomerDetails customerDetails, Long customerNumber);

    void deleteCustomer(Long customerNumber);

    AccountInformation findAccountByNumber(Long accountNumber);

    AccountInformation addNewAccount(AccountInformation accountInformation, Long customerNumber);

    void transferAmount(TransferDetails transferDetails, Long customerNumber);

    List<TransactionDetails> findTransactionsByAccountNumber(Long accountNumber);
    
}
