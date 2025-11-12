package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entities.Accounts;
import com.eazybytes.accounts.entities.Customer;
import com.eazybytes.accounts.exceptions.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exceptions.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService
{
  private CustomerRepository customerRepository;
  private AccountsRepository accountsRepository;

    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer =CustomerMapper.mapToCustomer(customerDto,new Customer());
        Optional<Customer> customerExists = customerRepository.findByMobileNumber(customer.getMobileNumber());
        if(customerExists.isPresent()){
          throw new CustomerAlreadyExistsException("Customer already exists with this mobile number "+customer.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        Accounts newAccount = createNewAccount(savedCustomer);
        accountsRepository.save(newAccount);
    }
    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }
    public CustomerDto getCustomerDetails(String mobileNumber)
    {
      Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
              ()->new ResourceNotFoundException("Customer","MobileNumber",mobileNumber)
      );
     Accounts account = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
             ()-> new ResourceNotFoundException("Accounts","Customer",customer.getCustomerId().toString())
     );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(account,new AccountsDto()));
        return customerDto;
    }
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isAccountUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto!=null) {
            Accounts account = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Accounts", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, account);
            accountsRepository.save(account);
            Customer customer = customerRepository.findById(account.getCustomerId()).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerId", account.getCustomerId().toString())
            );
            CustomerMapper.mapToCustomer(customerDto, customer);
            isAccountUpdated = true;
        }
            return isAccountUpdated;
        }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()->new ResourceNotFoundException("Customer","MobileNumber",mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }
}


