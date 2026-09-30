package com.challenge.repository;

import com.challenge.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    Optional<LedgerTransaction> findByTypeAndIdempotencyKey(String type, String idempotencyKey);
}
