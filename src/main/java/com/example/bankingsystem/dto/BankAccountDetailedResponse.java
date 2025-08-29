package com.example.bankingsystem.dto;

import com.example.bankingsystem.entity.BankAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDetailedResponse {

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

    // Computed fields
    private long accountAgeInDays;
    private String accountType;
    private boolean isRecentlyUpdated;
    private String formattedBalance;
    private String lastActivityStatus;

    public static BankAccountDetailedResponse fromEntity(BankAccount account) {
        LocalDateTime now = LocalDateTime.now();
        long accountAgeInDays = ChronoUnit.DAYS.between(account.getCreatedAt(), now);
        boolean isRecentlyUpdated = account.getUpdatedAt() != null &&
                ChronoUnit.HOURS.between(account.getUpdatedAt(), now) < 24;

        String accountType = determineAccountType(account.getBalance());
        String formattedBalance = formatBalance(account.getBalance(), account.getCurrency());
        String lastActivityStatus = determineActivityStatus(account.getUpdatedAt(), now);

        return BankAccountDetailedResponse.builder()
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
                .accountAgeInDays(accountAgeInDays)
                .accountType(accountType)
                .isRecentlyUpdated(isRecentlyUpdated)
                .formattedBalance(formattedBalance)
                .lastActivityStatus(lastActivityStatus)
                .build();
    }

    private static String determineAccountType(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return "PREMIUM";
        } else if (balance.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return "STANDARD";
        } else if (balance.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return "BASIC";
        } else {
            return "ENTRY";
        }
    }

    private static String formatBalance(BigDecimal balance, String currency) {
        return String.format("%s %.2f", currency, balance);
    }

    private static String determineActivityStatus(LocalDateTime updatedAt, LocalDateTime now) {
        if (updatedAt == null) {
            return "NEVER_UPDATED";
        }

        long hoursSinceUpdate = ChronoUnit.HOURS.between(updatedAt, now);

        if (hoursSinceUpdate < 1) {
            return "VERY_RECENT";
        } else if (hoursSinceUpdate < 24) {
            return "RECENT";
        } else if (hoursSinceUpdate < 168) { // 7 days
            return "MODERATE";
        } else {
            return "STALE";
        }
    }
}
