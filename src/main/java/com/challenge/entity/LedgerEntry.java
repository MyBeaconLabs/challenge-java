package com.challenge.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction transaction;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    /** Signed amount in minor units: positive credits the wallet, negative debits it. */
    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    protected LedgerEntry() {}

    LedgerEntry(LedgerTransaction transaction, Long walletId, long amountMinor) {
        this.transaction = transaction;
        this.walletId = walletId;
        this.amountMinor = amountMinor;
    }

    public Long getId() {
        return id;
    }

    public LedgerTransaction getTransaction() {
        return transaction;
    }

    public Long getWalletId() {
        return walletId;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
