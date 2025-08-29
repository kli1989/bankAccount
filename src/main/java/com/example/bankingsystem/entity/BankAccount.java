package com.example.bankingsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "accountNumber")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Account number is required")
    @Size(min = 8, max = 20, message = "Account number must be between 8 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Account number must contain only alphanumeric characters")
    private String accountNumber;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    private String accountHolderName;

    @Column(length = 255)
    @Email(message = "Email should be valid")
    private String email;

    @Column(length = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Phone number should be valid")
    private String phoneNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Balance must have at most 13 integer digits and 2 fraction digits")
    private BigDecimal balance;

    @Column(length = 3)
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = generateAccountId();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Generates a unique account ID that's hard to guess
     * Format: ACC + 12-character random alphanumeric string
     * Example: ACCA1B2C3D4E5
     */
    private static String generateAccountId() {
        String prefix = "ACC";
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(prefix);

        for (int i = 0; i < 12; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        CLOSED
    }
}
