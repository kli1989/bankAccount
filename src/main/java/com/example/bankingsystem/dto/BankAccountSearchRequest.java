package com.example.bankingsystem.dto;

import com.example.bankingsystem.entity.BankAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountSearchRequest {

    // Text search
    private String accountHolderName;
    private String accountNumber;

    // Filters
    private BankAccount.AccountStatus status;
    private String currency;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;

    // Date range filters
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    // Pagination and sorting (can be moved to separate params, but kept here for consistency)
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "DESC";

    /**
     * Check if any search criteria is provided
     */
    public boolean hasSearchCriteria() {
        return accountHolderName != null && !accountHolderName.trim().isEmpty() ||
               accountNumber != null && !accountNumber.trim().isEmpty() ||
               status != null ||
               currency != null && !currency.trim().isEmpty() ||
               minBalance != null ||
               maxBalance != null ||
               createdFrom != null ||
               createdTo != null ||
               updatedFrom != null ||
               updatedTo != null;
    }

    /**
     * Check if only pagination parameters are provided (equivalent to getAllAccounts)
     */
    public boolean isPaginationOnly() {
        return !hasSearchCriteria();
    }
}
