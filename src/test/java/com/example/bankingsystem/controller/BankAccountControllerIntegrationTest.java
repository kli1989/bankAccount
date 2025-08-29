package com.example.bankingsystem.controller;

import com.example.bankingsystem.dto.*;
import com.example.bankingsystem.entity.BankAccount;
import com.example.bankingsystem.repository.BankAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class BankAccountControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private BankAccount testAccount;
    private BankAccountRequest createRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up database
        bankAccountRepository.deleteAll();

        createRequest = BankAccountRequest.builder()
                .accountNumber("1234567890")
                .accountHolderName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .initialBalance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .build();

        testAccount = BankAccount.builder()
                .accountNumber("1234567890")
                .accountHolderName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .balance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .status(BankAccount.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAccount_ShouldCreateAccountSuccessfully() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", is("1234567890")))
                .andExpect(jsonPath("$.accountHolderName", is("John Doe")))
                .andExpect(jsonPath("$.balance", is(1000.00)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void createAccount_ShouldReturnConflict_WhenAccountNumberExists() throws Exception {
        // First create an account
        bankAccountRepository.save(testAccount);

        // Try to create another account with the same number
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Duplicate Account")))
                .andExpect(jsonPath("$.message", containsString("Account already exists")));
    }



    @Test
    void updateAccountByAccountNumber_ShouldUpdateAccountSuccessfully() throws Exception {
        BankAccount savedAccount = bankAccountRepository.save(testAccount);

        BankAccountUpdateRequest updateRequest = BankAccountUpdateRequest.builder()
                .accountHolderName("John Smith")
                .email("john.smith@example.com")
                .phoneNumber("1987654321")
                .build();

        mockMvc.perform(put("/accounts/number/{accountNumber}", savedAccount.getAccountNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountHolderName", is("John Smith")))
                .andExpect(jsonPath("$.email", is("john.smith@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("1987654321")));
    }

    @Test
    void deleteAccount_ShouldDeleteAccountSuccessfully() throws Exception {
        // Set balance to zero for successful deletion
        testAccount.setBalance(BigDecimal.ZERO);
        BankAccount savedAccount = bankAccountRepository.save(testAccount);

        mockMvc.perform(delete("/accounts/{id}", savedAccount.getId()))
                .andExpect(status().isNoContent());

        // Verify account is deleted by trying to get it by account number
        mockMvc.perform(get("/accounts/number/{accountNumber}", savedAccount.getAccountNumber()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAccount_ShouldReturnBadRequest_WhenAccountHasBalance() throws Exception {
        BankAccount savedAccount = bankAccountRepository.save(testAccount);

        mockMvc.perform(delete("/accounts/{id}", savedAccount.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bank Account Error")))
                .andExpect(jsonPath("$.message", containsString("Cannot delete account with positive balance")));
    }



    @Test
    void transferFunds_ShouldTransferSuccessfully() throws Exception {
        // Create source account
        BankAccount sourceAccount = bankAccountRepository.save(testAccount);

        // Create destination account
        BankAccount destAccount = BankAccount.builder()
                .accountNumber("0987654321")
                .accountHolderName("Jane Doe")
                .email("jane.doe@example.com")
                .phoneNumber("1987654321")
                .balance(BigDecimal.valueOf(500.00))
                .currency("USD")
                .status(BankAccount.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        bankAccountRepository.save(destAccount);

        FundTransferRequest transferRequest = FundTransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("0987654321")
                .amount(BigDecimal.valueOf(200.00))
                .description("Test transfer")
                .build();

        mockMvc.perform(post("/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is("true")))
                .andExpect(jsonPath("$.message", is("Fund transfer completed successfully")))
                .andExpect(jsonPath("$.fromAccount", is("1234567890")))
                .andExpect(jsonPath("$.toAccount", is("0987654321")))
                .andExpect(jsonPath("$.amount", is("200.0")));
    }

    @Test
    void transferFunds_ShouldReturnBadRequest_WhenInsufficientFunds() throws Exception {
        // Create source account with low balance
        testAccount.setBalance(BigDecimal.valueOf(100.00));
        bankAccountRepository.save(testAccount);

        // Create destination account
        BankAccount destAccount = BankAccount.builder()
                .accountNumber("0987654321")
                .accountHolderName("Jane Doe")
                .email("jane.doe@example.com")
                .phoneNumber("1987654321")
                .balance(BigDecimal.valueOf(500.00))
                .currency("USD")
                .status(BankAccount.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        bankAccountRepository.save(destAccount);

        FundTransferRequest transferRequest = FundTransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("0987654321")
                .amount(BigDecimal.valueOf(200.00))
                .build();

        mockMvc.perform(post("/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Insufficient Funds")))
                .andExpect(jsonPath("$.message", containsString("Insufficient funds")));
    }



    @Test
    void createAccount_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        BankAccountRequest invalidRequest = BankAccountRequest.builder()
                .accountNumber("") // Invalid: empty
                .accountHolderName("") // Invalid: empty
                .initialBalance(BigDecimal.valueOf(-100.00)) // Invalid: negative
                .currency("INVALID") // Invalid: not 3 letters
                .build();

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }

    @Nested
    @Transactional
    class AccountDetailsEndpointTests {

        private BankAccount testAccount;

        @BeforeEach
        void setUp() {
            // Clean up any existing data
            bankAccountRepository.deleteAll();

            // Create test account
            BankAccountRequest request = BankAccountRequest.builder()
                    .accountNumber("TEST123456789")
                    .accountHolderName("Test User")
                    .email("test@example.com")
                    .phoneNumber("1234567890")
                    .initialBalance(BigDecimal.valueOf(5000.00))
                    .currency("USD")
                    .build();

            BankAccount account = BankAccount.builder()
                    .accountNumber(request.getAccountNumber())
                    .accountHolderName(request.getAccountHolderName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .balance(request.getInitialBalance())
                    .currency(request.getCurrency())
                    .status(BankAccount.AccountStatus.ACTIVE)
                    .build();

            testAccount = bankAccountRepository.save(account);
        }

        @Test
        void getAccountById_ShouldReturnAccount() throws Exception {
            mockMvc.perform(get("/accounts/{id}", testAccount.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testAccount.getId())))
                    .andExpect(jsonPath("$.accountNumber", is("TEST123456789")))
                    .andExpect(jsonPath("$.accountHolderName", is("Test User")))
                    .andExpect(jsonPath("$.balance", is(5000.00)));
        }

        @Test
        void getAccountByAccountNumber_ShouldReturnAccount() throws Exception {
            mockMvc.perform(get("/accounts/number/{accountNumber}", "TEST123456789"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testAccount.getId())))
                    .andExpect(jsonPath("$.accountNumber", is("TEST123456789")))
                    .andExpect(jsonPath("$.accountHolderName", is("Test User")))
                    .andExpect(jsonPath("$.balance", is(5000.00)));
        }

        @Test
        void getAccountDetails_ShouldReturnDetailedAccountInfo() throws Exception {
            mockMvc.perform(get("/accounts/number/{accountNumber}/details", "TEST123456789"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testAccount.getId())))
                    .andExpect(jsonPath("$.accountNumber", is("TEST123456789")))
                    .andExpect(jsonPath("$.accountHolderName", is("Test User")))
                    .andExpect(jsonPath("$.balance", is(5000.00)))
                    .andExpect(jsonPath("$.accountType", is("STANDARD"))) // 5000 >= 1000
                    .andExpect(jsonPath("$.formattedBalance", is("USD 5000.00")))
                    .andExpect(jsonPath("$.accountAgeInDays", greaterThanOrEqualTo(0)));
        }

        @Test
        void getAccountById_WithInvalidId_ShouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/accounts/{id}", "INVALID_ID"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Account Not Found")));
        }

        @Test
        void getAccountByAccountNumber_WithInvalidNumber_ShouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/accounts/number/{accountNumber}", "INVALID_NUMBER"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Account Not Found")));
        }

        @Test
        void getAccountDetails_WithInvalidNumber_ShouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/accounts/number/{accountNumber}/details", "INVALID_NUMBER"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Account Not Found")));
        }
    }

    @Nested
    @Transactional
    class PaginationEndpointTests {

        @BeforeEach
        void setUp() {
            // Clean up any existing data
            bankAccountRepository.deleteAll();

            // Create test data
            for (int i = 1; i <= 25; i++) {
                // Create valid account numbers (8-20 characters)
                String accountNumber = "ACC" + String.format("%06d", i);
                // Create valid phone numbers (alphanumeric only, no special characters)
                String phoneNumber = "1" + String.format("%010d", i);

                BankAccount account = BankAccount.builder()
                        .accountNumber(accountNumber)
                        .accountHolderName("Test User " + i)
                        .email("user" + i + "@example.com")
                        .phoneNumber(phoneNumber)
                        .balance(BigDecimal.valueOf(1000 + i))
                        .currency("USD")
                        .status(BankAccount.AccountStatus.ACTIVE)
                        .build();

                bankAccountRepository.save(account);
            }
        }

        @Test
        void getAllAccounts_ShouldReturnFirstPage() throws Exception {
            mockMvc.perform(get("/accounts")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortBy", "createdAt")
                            .param("sortDir", "DESC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.pageNumber", is(0)))
                    .andExpect(jsonPath("$.pageSize", is(10)))
                    .andExpect(jsonPath("$.totalElements", is(25)))
                    .andExpect(jsonPath("$.totalPages", is(3)))
                    .andExpect(jsonPath("$.first", is(true)))
                    .andExpect(jsonPath("$.last", is(false)))
                    .andExpect(jsonPath("$.empty", is(false)));
        }

        @Test
        void getAllAccounts_ShouldReturnLastPage() throws Exception {
            mockMvc.perform(get("/accounts")
                            .param("page", "2")
                            .param("size", "10")
                            .param("sortBy", "createdAt")
                            .param("sortDir", "DESC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5)))
                    .andExpect(jsonPath("$.pageNumber", is(2)))
                    .andExpect(jsonPath("$.pageSize", is(10)))
                    .andExpect(jsonPath("$.totalElements", is(25)))
                    .andExpect(jsonPath("$.totalPages", is(3)))
                    .andExpect(jsonPath("$.first", is(false)))
                    .andExpect(jsonPath("$.last", is(true)))
                    .andExpect(jsonPath("$.empty", is(false)));
        }

        @Test
        void searchAccountsByName_ShouldReturnMatchingAccounts() throws Exception {
            mockMvc.perform(get("/accounts/search")
                            .param("accountHolderName", "Test User")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.content[0].accountHolderName", is("Test User 25")))
                    .andExpect(jsonPath("$.totalElements", is(25)));
        }

        @Test
        void searchAccountsByStatus_ShouldReturnActiveAccounts() throws Exception {
            mockMvc.perform(get("/accounts/search")
                            .param("status", "ACTIVE")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements", is(25)));
        }

        @Test
        void searchAccountsByCurrency_ShouldReturnMatchingCurrency() throws Exception {
            mockMvc.perform(get("/accounts/search")
                            .param("currency", "USD")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.content[0].currency", is("USD")))
                    .andExpect(jsonPath("$.totalElements", is(25)));
        }

        @Test
        void searchAccountsWithComplexCriteria_ShouldReturnFilteredResults() throws Exception {
            // Search for a specific user name that should exist
            mockMvc.perform(get("/accounts/search")
                            .param("accountHolderName", "Test User 5")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].accountHolderName", is("Test User 5")))
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }

        @Test
        void searchAccountsWithBalanceRange_ShouldReturnFilteredResults() throws Exception {
            // All accounts have balances between 1000 and 1025, so this should return all
            mockMvc.perform(get("/accounts/search")
                            .param("minBalance", "1000")
                            .param("maxBalance", "2000")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(10)))
                    .andExpect(jsonPath("$.totalElements", is(25)));
        }
    }
}
