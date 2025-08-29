package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.*;
import com.example.bankingsystem.entity.BankAccount;
import com.example.bankingsystem.exception.*;
import com.example.bankingsystem.repository.BankAccountRepository;
import com.example.bankingsystem.repository.BankAccountSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "account-details"}, allEntries = true)
    public BankAccountResponse createAccount(BankAccountRequest request) {
        log.info("Creating new account with number: {}", request.getAccountNumber());

        // Check for duplicate account number
        if (bankAccountRepository.findByAccountNumber(request.getAccountNumber()).isPresent()) {
            throw new DuplicateAccountException(request.getAccountNumber());
        }

        BankAccount account = BankAccount.builder()
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .balance(request.getInitialBalance())
                .currency(request.getCurrency())
                .status(BankAccount.AccountStatus.ACTIVE)
                .build();

        BankAccount savedAccount = bankAccountRepository.save(account);
        log.info("Account created successfully with ID: {}", savedAccount.getId());

        return BankAccountResponse.fromEntity(savedAccount);
    }



    @Override
    @Cacheable(value = "accounts", key = "#accountNumber")
    public BankAccountResponse getAccountByAccountNumber(String accountNumber) {
        log.debug("Fetching account by account number: {}", accountNumber);

        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return BankAccountResponse.fromEntity(account);
    }

    @Override
    @Cacheable(value = "accounts", key = "#id")
    public BankAccountResponse getAccountById(String id) {
        log.debug("Fetching account by ID: {}", id);

        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("ID: " + id));

        return BankAccountResponse.fromEntity(account);
    }

    @Override
    @Cacheable(value = "account-details", key = "#accountNumber")
    public BankAccountDetailedResponse getAccountDetails(String accountNumber) {
        log.debug("Fetching detailed account information for account number: {}", accountNumber);

        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return BankAccountDetailedResponse.fromEntity(account);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "account-details"}, key = "#accountNumber")
    public BankAccountResponse updateAccountByAccountNumber(String accountNumber, BankAccountUpdateRequest request) {
        log.info("Updating account with account number: {}", accountNumber);

        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        // Check if account is active
        if (account.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getAccountNumber(), account.getStatus());
        }

        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhoneNumber(request.getPhoneNumber());

        BankAccount updatedAccount = bankAccountRepository.save(account);
        log.info("Account updated successfully with account number: {}", updatedAccount.getAccountNumber());

        return BankAccountResponse.fromEntity(updatedAccount);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "account-details"}, allEntries = true)
    public void deleteAccount(String id) {
        log.info("Deleting account with ID: {}", id);

        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("ID: " + id));

        // Check if account has balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BankAccountException("Cannot delete account with positive balance. Current balance: " + account.getBalance());
        }

        bankAccountRepository.delete(account);
        log.info("Account deleted successfully with ID: {}", id);
    }



    @Override
    @Transactional
    public boolean transferFunds(FundTransferRequest request) {
        log.info("Processing fund transfer from {} to {} for amount: {}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        // Prevent self-transfer
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new BankAccountException("Cannot transfer funds to the same account");
        }

        // Use pessimistic locking to prevent concurrent modification issues
        // Lock accounts in consistent order to prevent deadlocks
        List<String> accountNumbers = Arrays.asList(request.getFromAccountNumber(), request.getToAccountNumber());
        accountNumbers.sort(Comparator.naturalOrder());

        BankAccount fromAccount = bankAccountRepository.findByAccountNumberWithLock(accountNumbers.get(0))
                .orElseThrow(() -> new AccountNotFoundException(accountNumbers.get(0)));

        BankAccount toAccount = bankAccountRepository.findByAccountNumberWithLock(accountNumbers.get(1))
                .orElseThrow(() -> new AccountNotFoundException(accountNumbers.get(1)));

        // Verify the accounts are the correct ones (since we sorted)
        if (fromAccount.getAccountNumber().equals(request.getFromAccountNumber())) {
            // Accounts are in correct order
        } else {
            // Swap accounts
            BankAccount temp = fromAccount;
            fromAccount = toAccount;
            toAccount = temp;
        }

        // Validate accounts are active
        if (fromAccount.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountInactiveException(fromAccount.getAccountNumber(), fromAccount.getStatus());
        }

        if (toAccount.getStatus() != BankAccount.AccountStatus.ACTIVE) {
            throw new AccountInactiveException(toAccount.getAccountNumber(), toAccount.getStatus());
        }

        // Validate currencies match
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new BankAccountException("Currency mismatch: source account is in " +
                    fromAccount.getCurrency() + ", destination account is in " + toAccount.getCurrency());
        }

        // Check sufficient funds
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(fromAccount.getAccountNumber(),
                    request.getAmount(), fromAccount.getBalance());
        }

        // Perform the transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        log.info("Fund transfer completed successfully from {} to {} for amount: {}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        return true;
    }

    @Override
    public BankAccountPagedResponse searchAccounts(BankAccountSearchRequest searchRequest) {
        log.debug("Searching accounts with complex criteria: {}", searchRequest);

        // Create pageable from search request
        Sort.Direction direction = Sort.Direction.fromString(searchRequest.getSortDir().toUpperCase());
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(direction, searchRequest.getSortBy())
        );

        Page<BankAccount> accountPage;

        // If no search criteria provided, return all accounts (equivalent to getAllAccounts)
        if (searchRequest.isPaginationOnly()) {
            log.debug("No search criteria provided, fetching all accounts");
            accountPage = bankAccountRepository.findAll(pageable);
        } else {
            // Use dynamic specification for complex search
            log.debug("Applying search criteria: {}", searchRequest);
            Specification<BankAccount> spec = BankAccountSpecification.createSpecification(searchRequest);
            accountPage = bankAccountRepository.findAll(spec, pageable);
        }

        Page<BankAccountResponse> responsePage = accountPage.map(BankAccountResponse::fromEntity);
        return BankAccountPagedResponse.fromPage(responsePage);
    }

}
