package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CardsDto;
import com.eazybytes.accounts.dto.CustomerDetailsDto;
import com.eazybytes.accounts.dto.LoansDto;
import com.eazybytes.accounts.entities.Accounts;
import com.eazybytes.accounts.entities.Customer;
import com.eazybytes.accounts.exceptions.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.ICustomerService;
import com.eazybytes.accounts.service.client.CardsFeignClient;
import com.eazybytes.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements ICustomerService
{
     private AccountsRepository accountsRepository;
     private CustomerRepository customerRepository;
     private LoansFeignClient loansFeignClient;
     private CardsFeignClient cardsFeignClient;
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer existingCustomer= customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer","MobileNumber",mobileNumber)
        );
        Accounts existingAccounts =accountsRepository.findByCustomerId(existingCustomer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account","Customer",existingCustomer.getCustomerId().toString())
        );
        ResponseEntity<LoansDto> existingLoans = loansFeignClient.fetchLoan(mobileNumber);
        ResponseEntity<CardsDto> existingCards = cardsFeignClient.fetchCardsDetails(mobileNumber);
        CustomerDetailsDto customerDetailsDto= CustomerMapper.mapToCustomerDetailsDto(new CustomerDetailsDto(),existingCustomer);
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(existingAccounts,new AccountsDto()));
        customerDetailsDto.setLoansDto(existingLoans.getBody());
        customerDetailsDto.setCardsDto(existingCards.getBody());

        return customerDetailsDto;
    }
}
