package com.challenge.repository;

import com.challenge.entity.Wallet;
import com.challenge.entity.WalletType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Locks the given wallets with SELECT ... FOR UPDATE. Rows are locked in id order,
     * so concurrent postings touching the same wallets cannot deadlock each other.
     * Must be called inside a transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id in :ids order by w.id")
    List<Wallet> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);

    Optional<Wallet> findByTypeAndCurrency(WalletType type, String currency);

    List<Wallet> findByUserIdOrderById(Long userId);

    boolean existsByUserIdAndCurrency(Long userId, String currency);
}
