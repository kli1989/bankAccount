package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.*;
import com.example.bankingsystem.entity.BankAccount;
import com.example.bankingsystem.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping("/accounts")
    public ResponseEntity<BankAccountResponse> createAccount(@Valid @RequestBody BankAccountRequest request) {
        log.info("Creating new account with number: {}", request.getAccountNumber());
        BankAccountResponse response = bankAccountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/accounts/number/{accountNumber}")
    public ResponseEntity<BankAccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        log.debug("Fetching account by account number: {}", accountNumber);
        BankAccountResponse response = bankAccountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccountResponse> getAccountById(@PathVariable String id) {
        log.debug("Fetching account by ID: {}", id);
        BankAccountResponse response = bankAccountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/number/{accountNumber}/details")
    public ResponseEntity<BankAccountDetailedResponse> getAccountDetails(@PathVariable String accountNumber) {
        log.debug("Fetching detailed account information for account number: {}", accountNumber);
        BankAccountDetailedResponse response = bankAccountService.getAccountDetails(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/number/{accountNumber}")
    public ResponseEntity<BankAccountResponse> updateAccountByAccountNumber(
            @PathVariable String accountNumber,
            @Valid @RequestBody BankAccountUpdateRequest request) {
        log.info("Updating account with account number: {}", accountNumber);
        BankAccountResponse response = bankAccountService.updateAccountByAccountNumber(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        log.info("Deleting account with ID: {}", id);
        bankAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/accounts/transfer")
    public ResponseEntity<Map<String, String>> transferFunds(@Valid @RequestBody FundTransferRequest request) {
        log.info("Processing fund transfer from {} to {} for amount: {}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        boolean success = bankAccountService.transferFunds(request);

        Map<String, String> response = Map.of(
                "message", "Fund transfer completed successfully",
                "fromAccount", request.getFromAccountNumber(),
                "toAccount", request.getToAccountNumber(),
                "amount", request.getAmount().toString(),
                "success", String.valueOf(success)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts")
    public ResponseEntity<BankAccountPagedResponse> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.debug("Fetching all accounts with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        // Use the unified search endpoint with no criteria (equivalent to getAllAccounts)
        BankAccountSearchRequest searchRequest = BankAccountSearchRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        BankAccountPagedResponse response = bankAccountService.searchAccounts(searchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/search")
    public ResponseEntity<BankAccountPagedResponse> searchAccounts(
            @RequestParam(required = false) String accountHolderName,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) BankAccount.AccountStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.debug("Searching accounts with complex criteria - name: {}, accountNumber: {}, status: {}, currency: {}, minBalance: {}, maxBalance: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                accountHolderName, accountNumber, status, currency, minBalance, maxBalance, page, size, sortBy, sortDir);

        BankAccountSearchRequest searchRequest = BankAccountSearchRequest.builder()
                .accountHolderName(accountHolderName)
                .accountNumber(accountNumber)
                .status(status)
                .currency(currency)
                .minBalance(minBalance)
                .maxBalance(maxBalance)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        BankAccountPagedResponse response = bankAccountService.searchAccounts(searchRequest);
        return ResponseEntity.ok(response);
    }



}
