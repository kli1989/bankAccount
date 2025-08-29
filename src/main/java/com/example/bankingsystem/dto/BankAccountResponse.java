package com.example.bankingsystem.dto;

import com.example.bankingsystem.entity.BankAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {

    private String id;
    private String accountNumber;
    private String accountHolderName;
    private String email;
    private String phoneNumber;
    private BigDecimal balance;
    private String currency;
    private BankAccount.AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BankAccountResponse fromEntity(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
