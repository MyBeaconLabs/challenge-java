package com.challenge.service;

import java.util.List;

/**
 * A request to move money: a set of legs that must sum to zero.
 *
 * @param type           what kind of movement this is, e.g. DEPOSIT
 * @param idempotencyKey unique per type; posting the same key twice returns the original transaction
 * @param legs           signed amounts in minor units; positive credits the wallet, negative debits it
 */
public record Posting(String type, String idempotencyKey, String description, List<Leg> legs) {

    public record Leg(Long walletId, long amountMinor) {}
}
