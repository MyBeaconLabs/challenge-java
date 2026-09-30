package com.challenge.controller;

import com.challenge.dto.CreateWalletRequest;
import com.challenge.dto.DepositRequest;
import com.challenge.dto.LedgerEntryResponse;
import com.challenge.dto.WalletResponse;
import com.challenge.entity.Wallet;
import com.challenge.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@Validated
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return WalletResponse.from(walletService.createWallet(request.userId(), request.currency()));
    }

    @GetMapping("/{id}")
    public WalletResponse getWallet(@PathVariable Long id) {
        return WalletResponse.from(walletService.getWallet(id));
    }

    @GetMapping("/{id}/entries")
    public List<LedgerEntryResponse> getEntries(@PathVariable Long id,
                                                @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        Wallet wallet = walletService.getWallet(id);
        return walletService.getEntries(id, limit).stream()
            .map(entry -> LedgerEntryResponse.from(entry, wallet.getCurrency()))
            .toList();
    }

    /**
     * Internal: called by the funding pipeline when money arrives for a wallet.
     * Idempotent on the Idempotency-Key header.
     */
    @PostMapping("/{id}/deposits")
    public WalletResponse deposit(@PathVariable Long id,
                                  @RequestHeader("Idempotency-Key") String idempotencyKey,
                                  @Valid @RequestBody DepositRequest request) {
        return WalletResponse.from(walletService.deposit(id, request.amount(), idempotencyKey));
    }
}
