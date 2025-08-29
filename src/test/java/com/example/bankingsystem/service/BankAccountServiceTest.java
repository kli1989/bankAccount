package com.example.bankingsystem.service;

import com.example.bankingsystem.dto.BankAccountRequest;
import com.example.bankingsystem.dto.BankAccountResponse;
import com.example.bankingsystem.dto.BankAccountUpdateRequest;
import com.example.bankingsystem.dto.FundTransferRequest;
import com.example.bankingsystem.entity.BankAccount;
import com.example.bankingsystem.exception.*;
import com.example.bankingsystem.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    private BankAccount testAccount;
    private BankAccountRequest createRequest;
    private BankAccountUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAccount = BankAccount.builder()
                .id("ACC1234567890")
                .accountNumber("1234567890")
                .accountHolderName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .balance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .status(BankAccount.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = BankAccountRequest.builder()
                .accountNumber("1234567890")
                .accountHolderName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .initialBalance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .build();

        updateRequest = BankAccountUpdateRequest.builder()
                .accountHolderName("John Smith")
                .email("john.smith@example.com")
                .phoneNumber("+0987654321")
                .build();
    }

    @Test
    void createAccount_ShouldCreateAccountSuccessfully() {
        // Given
        given(bankAccountRepository.findByAccountNumber(anyString())).willReturn(Optional.empty());
        given(bankAccountRepository.save(any(BankAccount.class))).willReturn(testAccount);

        // When
        BankAccountResponse response = bankAccountService.createAccount(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getAccountHolderName()).isEqualTo("John Doe");
        assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(1000.00));
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_ShouldThrowDuplicateAccountException_WhenAccountNumberExists() {
        // Given
        given(bankAccountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> bankAccountService.createAccount(createRequest))
                .isInstanceOf(DuplicateAccountException.class)
                .hasMessage("Account already exists with account number: 1234567890");
    }



    @Test
    void updateAccountByAccountNumber_ShouldUpdateAccountSuccessfully() {
        // Given
        given(bankAccountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));
        given(bankAccountRepository.save(any(BankAccount.class))).willReturn(testAccount);

        // When
        BankAccountResponse response = bankAccountService.updateAccountByAccountNumber("1234567890", updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void updateAccountByAccountNumber_ShouldThrowAccountInactiveException_WhenAccountIsInactive() {
        // Given
        testAccount.setStatus(BankAccount.AccountStatus.INACTIVE);
        given(bankAccountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> bankAccountService.updateAccountByAccountNumber("1234567890", updateRequest))
                .isInstanceOf(AccountInactiveException.class);
    }

    @Test
    void deleteAccount_ShouldDeleteAccountSuccessfully() {
        // Given
        testAccount.setBalance(BigDecimal.ZERO);
        given(bankAccountRepository.findById("ACC1234567890")).willReturn(Optional.of(testAccount));

        // When
        bankAccountService.deleteAccount("ACC1234567890");

        // Then
        verify(bankAccountRepository).delete(testAccount);
    }

    @Test
    void deleteAccount_ShouldThrowException_WhenAccountHasBalance() {
        // Given
        given(bankAccountRepository.findById("ACC1234567890")).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> bankAccountService.deleteAccount("ACC1234567890"))
                .isInstanceOf(BankAccountException.class)
                .hasMessageContaining("Cannot delete account with positive balance");
    }



    @Test
    void transferFunds_ShouldTransferSuccessfully() {
        // Given
        BankAccount fromAccount = testAccount;
        BankAccount toAccount = BankAccount.builder()
                .id("ACC0987654321")
                .accountNumber("0987654321")
                .accountHolderName("Jane Doe")
                .balance(BigDecimal.valueOf(500.00))
                .currency("USD")
                .status(BankAccount.AccountStatus.ACTIVE)
                .build();

        FundTransferRequest transferRequest = FundTransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("0987654321")
                .amount(BigDecimal.valueOf(200.00))
                .build();

        given(bankAccountRepository.findByAccountNumberWithLock("0987654321")).willReturn(Optional.of(toAccount));
        given(bankAccountRepository.findByAccountNumberWithLock("1234567890")).willReturn(Optional.of(fromAccount));

        // When
        boolean result = bankAccountService.transferFunds(transferRequest);

        // Then
        assertThat(result).isTrue();
        verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
    }

    @Test
    void transferFunds_ShouldThrowInsufficientFundsException_WhenBalanceIsLow() {
        // Given
        testAccount.setBalance(BigDecimal.valueOf(100.00));
        FundTransferRequest transferRequest = FundTransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("0987654321")
                .amount(BigDecimal.valueOf(200.00))
                .build();

        BankAccount toAccount = BankAccount.builder()
                .id("ACC0987654321")
                .accountNumber("0987654321")
                .accountHolderName("Jane Doe")
                .balance(BigDecimal.valueOf(500.00))
                .currency("USD")
                .status(BankAccount.AccountStatus.ACTIVE)
                .build();

        given(bankAccountRepository.findByAccountNumberWithLock("0987654321")).willReturn(Optional.of(toAccount));
        given(bankAccountRepository.findByAccountNumberWithLock("1234567890")).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> bankAccountService.transferFunds(transferRequest))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transferFunds_ShouldThrowException_WhenSelfTransfer() {
        // Given
        FundTransferRequest transferRequest = FundTransferRequest.builder()
                .fromAccountNumber("1234567890")
                .toAccountNumber("1234567890")
                .amount(BigDecimal.valueOf(200.00))
                .build();

        // When & Then
        assertThatThrownBy(() -> bankAccountService.transferFunds(transferRequest))
                .isInstanceOf(BankAccountException.class)
                .hasMessage("Cannot transfer funds to the same account");
    }

}
