package com.challenge.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "ledger_transactions")
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.PERSIST)
    @OrderBy("id")
    private List<LedgerEntry> entries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    protected LedgerTransaction() {}

    public LedgerTransaction(String type, String idempotencyKey, String description) {
        this.type = type;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
    }

    public void addEntry(Long walletId, long amountMinor) {
        entries.add(new LedgerEntry(this, walletId, amountMinor));
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<LedgerEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
