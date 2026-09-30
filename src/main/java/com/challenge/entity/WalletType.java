package com.challenge.entity;

public enum WalletType {
    /** Owned by a user. Balance can never go negative. */
    USER,
    /** Internal platform account, one per currency. Funds deposits. */
    SYSTEM
}
