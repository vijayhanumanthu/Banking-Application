package com.coding.exercise.bankapp.service.helper;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.AddressDetails;
import com.coding.exercise.bankapp.domain.BankInformation;
import com.coding.exercise.bankapp.domain.ContactDetails;
import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.domain.TransferDetails;
import com.coding.exercise.bankapp.model.Account;
import com.coding.exercise.bankapp.model.Address;
import com.coding.exercise.bankapp.model.BankInfo;
import com.coding.exercise.bankapp.model.Contact;
import com.coding.exercise.bankapp.model.Customer;
import com.coding.exercise.bankapp.model.Transaction;
@Component
public class BankingServiceHelper {

    public CustomerDetails convertToCustomerDomain(Customer customer) {
        if (customer == null) return null;

        CustomerDetails d = new CustomerDetails();
        d.setCustomerNumber(customer.getCustomerNumber());
        d.setFirstName(customer.getFirstName());
        d.setMiddleName(customer.getMiddleName());
        d.setLastName(customer.getLastName());
        d.setStatus(customer.getStatus());
        d.setContactDetails(convertToContactDomain(customer.getContactDetails()));
        d.setCustomerAddress(convertToAddressDomain(customer.getCustomerAddress()));
        return d;
    }

    public Customer convertToCustomerEntity(CustomerDetails details) {
        if (details == null) return null;

        Customer c = new Customer();
        c.setCustomerNumber(details.getCustomerNumber());
        c.setFirstName(details.getFirstName());
        c.setMiddleName(details.getMiddleName());
        c.setLastName(details.getLastName());
        c.setStatus(details.getStatus());
        c.setContactDetails(convertToContactEntity(details.getContactDetails()));
        c.setCustomerAddress(convertToAddressEntity(details.getCustomerAddress()));
        return c;
    }

    public ContactDetails convertToContactDomain(Contact contact) {
        if (contact == null) return null;

        ContactDetails d = new ContactDetails();
        d.setEmailId(contact.getEmailId());
        d.setHomePhone(contact.getHomePhone());
        d.setWorkPhone(contact.getWorkPhone());
        return d;
    }

    public Contact convertToContactEntity(ContactDetails details) {
        if (details == null) return null;

        Contact c = new Contact();
        c.setEmailId(details.getEmailId());
        c.setHomePhone(details.getHomePhone());
        c.setWorkPhone(details.getWorkPhone());
        return c;
    }

    public AddressDetails convertToAddressDomain(Address address) {
        if (address == null) return null;

        AddressDetails d = new AddressDetails();
        d.setAddress1(address.getAddress1());
        d.setAddress2(address.getAddress2());
        d.setCity(address.getCity());
        d.setState(address.getState());
        d.setZip(address.getZip());
        d.setCountry(address.getCountry());
        return d;
    }

    public Address convertToAddressEntity(AddressDetails details) {
        if (details == null) return null;

        Address a = new Address();
        a.setAddress1(details.getAddress1());
        a.setAddress2(details.getAddress2());
        a.setCity(details.getCity());
        a.setState(details.getState());
        a.setZip(details.getZip());
        a.setCountry(details.getCountry());
        return a;
    }

    public AccountInformation convertToAccountDomain(Account account) {
        if (account == null) return null;

        AccountInformation d = new AccountInformation();
        d.setAccountNumber(account.getAccountNumber());
        d.setAccountType(account.getAccountType());
        d.setAccountBalance(account.getAccountBalance());
        d.setBankInformation(convertToBankDomain(account.getBankInformation()));
        return d;
    }

    public Account convertToAccountEntity(AccountInformation info) {
        if (info == null) return null;

        Account a = new Account();
        a.setAccountNumber(info.getAccountNumber());
        a.setAccountType(info.getAccountType());
        a.setAccountBalance(info.getAccountBalance());
        a.setBankInformation(convertToBankEntity(info.getBankInformation()));
        a.setCreateDateTime(new Date());
        return a;
    }

    public BankInformation convertToBankDomain(BankInfo bank) {
        if (bank == null) return null;

        BankInformation d = new BankInformation();
        d.setBankName(bank.getBankName());
        d.setBranchName(bank.getBranchName());
        d.setIfscCode(bank.getIfscCode());
        return d;
    }

    public BankInfo convertToBankEntity(BankInformation info) {
        if (info == null) return null;

        BankInfo b = new BankInfo();
        b.setBankName(info.getBankName());
        b.setBranchName(info.getBranchName());
        b.setIfscCode(info.getIfscCode());
        return b;
    }

    public Transaction createTransaction(TransferDetails t, Long accNo, String type) {
        Transaction tx = new Transaction();
        tx.setAccountNumber(accNo);
        tx.setTransactionType(type);
        tx.setAmount(t.getTransferAmount());
        tx.setDescription(t.getRemarks());
        tx.setTransactionDate(new Date());
        return tx;
    }
}
