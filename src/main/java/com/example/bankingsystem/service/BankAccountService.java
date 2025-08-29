package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.*;
import com.example.bankingsystem.entity.BankAccount;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BankAccountService {

    /**
     * Create a new bank account
     * @param request the account creation request
     * @return the created account response
     */
    BankAccountResponse createAccount(BankAccountRequest request);



    /**
     * Get account by account number
     * @param accountNumber the account number
     * @return the account response
     */
    BankAccountResponse getAccountByAccountNumber(String accountNumber);

    /**
     * Get account by ID
     * @param id the account ID
     * @return the account response
     */
    BankAccountResponse getAccountById(String id);

    /**
     * Get detailed account information including computed fields
     * @param accountNumber the account number
     * @return detailed account response with additional computed fields
     */
    BankAccountDetailedResponse getAccountDetails(String accountNumber);

    /**
     * Update account details by account number
     * @param accountNumber the account number
     * @param request the update request
     * @return the updated account response
     */
    BankAccountResponse updateAccountByAccountNumber(String accountNumber, BankAccountUpdateRequest request);

    /**
     * Delete account by ID
     * @param id the account ID
     */
    void deleteAccount(String id);



    /**
     * Transfer funds between accounts
     * @param request the fund transfer request
     * @return true if transfer was successful
     */
    boolean transferFunds(FundTransferRequest request);

    /**
     * Search accounts with complex criteria including pagination
     * Supports filtering by account holder name, account number, status, currency,
     * balance range, and date ranges. Falls back to getAllAccounts when no criteria provided.
     * @param searchRequest the search criteria including pagination and sorting
     * @return paginated response of matching accounts
     */
    BankAccountPagedResponse searchAccounts(BankAccountSearchRequest searchRequest);

}
