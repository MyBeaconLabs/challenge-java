package com.challenge.service;

import com.challenge.dto.TransferRequest;
import com.challenge.entity.Transfer;
import com.challenge.entity.User;
import com.challenge.entity.Wallet;
import com.challenge.exception.InsufficientFundsException;
import com.challenge.exception.NotFoundException;
import com.challenge.repository.TransferRepository;
import com.challenge.repository.UserRepository;
import com.challenge.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public TransferService(TransferRepository transferRepository,
                           WalletRepository walletRepository,
                           UserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public Transfer createTransfer(TransferRequest request) {
        if (request.amount() == null || request.amount().equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        long amountMinor = request.amount().multiply(BigDecimal.valueOf(100)).longValue();

        Wallet from = walletRepository.findById(request.fromWalletId()).get();
        User sender = userRepository.findById(from.getUserId()).get();
        log.info("Processing transfer of {} from {} <{}> to wallet {}",
            request.amount(), sender.getName(), sender.getEmail(), request.toWalletId());

        if (amountMinor > from.getDailyTransferLimitMinor()) {
            throw new IllegalArgumentException("Transfer exceeds daily limit");
        }

        Transfer transfer = transferRepository.save(
            new Transfer(from.getId(), request.toWalletId(), amountMinor, request.note()));
        try {
            executeTransfer(transfer);
            transfer.setStatus("COMPLETED");
        } catch (RuntimeException e) {
            transfer.setStatus("FAILED");
            throw e;
        } finally {
            transferRepository.save(transfer);
        }
        return transfer;
    }

    @Transactional
    public void executeTransfer(Transfer transfer) {
        Wallet from = walletRepository.findById(transfer.getFromWalletId()).get();
        if (from.getBalanceMinor() < transfer.getAmountMinor()) {
            throw new InsufficientFundsException(from.getId());
        }
        from.setBalanceMinor(from.getBalanceMinor() - transfer.getAmountMinor());
        walletRepository.save(from);

        Wallet to = walletRepository.findById(transfer.getToWalletId())
            .orElseThrow(() -> new NotFoundException("Recipient wallet not found"));
        to.setBalanceMinor(to.getBalanceMinor() + transfer.getAmountMinor());
        walletRepository.save(to);
    }

    public Transfer getTransfer(Long id) {
        return transferRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer not found: " + id));
    }
}
