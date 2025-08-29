package com.example.bankingsystem.repository;

import com.example.bankingsystem.dto.BankAccountSearchRequest;
import com.example.bankingsystem.entity.BankAccount;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BankAccountSpecification {

    public static Specification<BankAccount> createSpecification(BankAccountSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text search for account holder name (case-insensitive)
            if (searchRequest.getAccountHolderName() != null && !searchRequest.getAccountHolderName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("accountHolderName")),
                    "%" + searchRequest.getAccountHolderName().toLowerCase().trim() + "%"
                ));
            }

            // Exact match for account number
            if (searchRequest.getAccountNumber() != null && !searchRequest.getAccountNumber().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("accountNumber"),
                    searchRequest.getAccountNumber().trim()
                ));
            }

            // Filter by status
            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("status"),
                    searchRequest.getStatus()
                ));
            }

            // Filter by currency
            if (searchRequest.getCurrency() != null && !searchRequest.getCurrency().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("currency"),
                    searchRequest.getCurrency().trim().toUpperCase()
                ));
            }

            // Balance range filters
            if (searchRequest.getMinBalance() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("balance"),
                    searchRequest.getMinBalance()
                ));
            }

            if (searchRequest.getMaxBalance() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("balance"),
                    searchRequest.getMaxBalance()
                ));
            }

            // Date range filters for created date
            if (searchRequest.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    searchRequest.getCreatedFrom()
                ));
            }

            if (searchRequest.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    searchRequest.getCreatedTo()
                ));
            }

            // Date range filters for updated date
            if (searchRequest.getUpdatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"),
                    searchRequest.getUpdatedFrom()
                ));
            }

            if (searchRequest.getUpdatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"),
                    searchRequest.getUpdatedTo()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
