package com.challenge.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_wallet_id", nullable = false)
    private Long fromWalletId;

    @Column(name = "to_wallet_id", nullable = false)
    private Long toWalletId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    protected Transfer() {}

    public Transfer(Long fromWalletId, Long toWalletId, long amountMinor, String note) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amountMinor = amountMinor;
        this.note = note;
        this.status = "PENDING";
    }

    public Long getId() { return id; }
    public Long getFromWalletId() { return fromWalletId; }
    public Long getToWalletId() { return toWalletId; }
    public long getAmountMinor() { return amountMinor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}
