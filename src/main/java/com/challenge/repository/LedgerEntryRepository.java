package com.challenge.repository;

import com.challenge.entity.LedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByWalletIdOrderByIdDesc(Long walletId, Pageable pageable);
}
