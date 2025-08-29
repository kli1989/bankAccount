package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.BankAccountRequest;
import com.example.bankingsystem.dto.FundTransferRequest;
import com.example.bankingsystem.entity.BankAccount;
import com.example.bankingsystem.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrentTransferStressTest {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private BankAccount sourceAccount;
    private BankAccount destinationAccount;

    @BeforeEach
    void setUp() {
        // Use unique account numbers for each test run to avoid conflicts
        String uniqueId = String.valueOf(System.currentTimeMillis() % 1000000);

        // Create source account with sufficient balance
        BankAccountRequest sourceRequest = BankAccountRequest.builder()
                .accountNumber("SOURCEACC" + uniqueId)
                .accountHolderName("Source User")
                .email("source" + uniqueId + "@example.com")
                .phoneNumber("1234567890")
                .initialBalance(BigDecimal.valueOf(10000.00))
                .currency("USD")
                .build();

        // Create destination account
        BankAccountRequest destRequest = BankAccountRequest.builder()
                .accountNumber("DESTACC" + uniqueId)
                .accountHolderName("Destination User")
                .email("dest" + uniqueId + "@example.com")
                .phoneNumber("0987654321")
                .initialBalance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .build();

        // Create the accounts (they should be new due to unique IDs)
        bankAccountService.createAccount(sourceRequest);
        bankAccountService.createAccount(destRequest);

        // Retrieve the created accounts
        sourceAccount = bankAccountRepository.findByAccountNumber("SOURCEACC" + uniqueId)
                .orElseThrow(() -> new RuntimeException("Failed to create source account"));
        destinationAccount = bankAccountRepository.findByAccountNumber("DESTACC" + uniqueId)
                .orElseThrow(() -> new RuntimeException("Failed to create destination account"));
    }

    @Test
    void concurrentTransfers_ShouldMaintainDataConsistency() throws Exception {
        int numberOfThreads = 10;
        int transfersPerThread = 20;
        BigDecimal transferAmount = BigDecimal.valueOf(10.00);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successfulTransfers = new AtomicInteger(0);
        AtomicInteger failedTransfers = new AtomicInteger(0);
        AtomicReference<Exception> lastException = new AtomicReference<>();

        // Record initial balances
        BigDecimal initialSourceBalance = sourceAccount.getBalance();
        BigDecimal initialDestBalance = destinationAccount.getBalance();

        // Submit concurrent transfer tasks
        CompletableFuture<?>[] futures = IntStream.range(0, numberOfThreads)
                .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                    for (int i = 0; i < transfersPerThread; i++) {
                        try {
                            FundTransferRequest request = FundTransferRequest.builder()
                                    .fromAccountNumber(sourceAccount.getAccountNumber())
                                    .toAccountNumber(destinationAccount.getAccountNumber())
                                    .amount(transferAmount)
                                    .description("Concurrent transfer " + threadId + "-" + i)
                                    .build();

                            boolean result = bankAccountService.transferFunds(request);
                            if (result) {
                                successfulTransfers.incrementAndGet();
                            } else {
                                failedTransfers.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedTransfers.incrementAndGet();
                            lastException.set(e);
                            System.err.println("Transfer failed: " + e.getMessage());
                        }
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        // Wait for all transfers to complete
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        // Verify final balances
        BigDecimal finalSourceBalance = bankAccountRepository.findByAccountNumber(sourceAccount.getAccountNumber())
                .orElseThrow().getBalance();
        BigDecimal finalDestBalance = bankAccountRepository.findByAccountNumber(destinationAccount.getAccountNumber())
                .orElseThrow().getBalance();

        BigDecimal expectedSourceBalance = initialSourceBalance.subtract(
                transferAmount.multiply(BigDecimal.valueOf(successfulTransfers.get())));
        BigDecimal expectedDestBalance = initialDestBalance.add(
                transferAmount.multiply(BigDecimal.valueOf(successfulTransfers.get())));

        System.out.println("Initial source balance: " + initialSourceBalance);
        System.out.println("Final source balance: " + finalSourceBalance);
        System.out.println("Expected source balance: " + expectedSourceBalance);
        System.out.println("Initial dest balance: " + initialDestBalance);
        System.out.println("Final dest balance: " + finalDestBalance);
        System.out.println("Expected dest balance: " + expectedDestBalance);
        System.out.println("Successful transfers: " + successfulTransfers.get());
        System.out.println("Failed transfers: " + failedTransfers.get());

        // Assertions
        assertThat(finalSourceBalance).isEqualTo(expectedSourceBalance);
        assertThat(finalDestBalance).isEqualTo(expectedDestBalance);
        assertThat(successfulTransfers.get() + failedTransfers.get())
                .isEqualTo(numberOfThreads * transfersPerThread);

        // If there were failures, they should be due to insufficient funds, not race conditions
        if (failedTransfers.get() > 0) {
            assertThat(lastException.get()).isNotNull();
            // The last exception should be InsufficientFundsException if transfers failed due to low balance
        }
    }

    @Test
    void concurrentBalanceReads_ShouldBeConsistent() throws Exception {
        int numberOfThreads = 20;
        int readsPerThread = 50;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger readCount = new AtomicInteger(0);

        String accountNumber = sourceAccount.getAccountNumber();

        // Submit concurrent balance read tasks
        CompletableFuture<?>[] futures = IntStream.range(0, numberOfThreads)
                .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                    for (int i = 0; i < readsPerThread; i++) {
                        try {
                            BigDecimal balance = bankAccountService.getAccountByAccountNumber(accountNumber).getBalance();
                            assertThat(balance).isNotNull();
                            assertThat(balance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
                            readCount.incrementAndGet();
                        } catch (Exception e) {
                            System.err.println("Balance read failed: " + e.getMessage());
                        }
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);

        // Wait for all reads to complete
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        // Verify all reads were successful
        assertThat(readCount.get()).isEqualTo(numberOfThreads * readsPerThread);
    }
}
