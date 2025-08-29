package com.example.bankingsystem.repository;

import com.example.bankingsystem.entity.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String>, JpaSpecificationExecutor<BankAccount> {

    /**
     * Find account by account number
     * @param accountNumber the account number
     * @return Optional containing the account if found
     */
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /**
     * Find account by account number with pessimistic write lock for concurrent operations
     * @param accountNumber the account number
     * @return Optional containing the account if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccount a WHERE a.accountNumber = :accountNumber")
    Optional<BankAccount> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    /**
     * Find all accounts with pagination
     * @param pageable pagination information
     * @return Page of accounts
     */
    Page<BankAccount> findAll(Pageable pageable);

}
