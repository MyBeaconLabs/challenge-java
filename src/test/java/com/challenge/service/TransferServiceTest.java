package com.challenge.service;

import com.challenge.dto.TransferRequest;
import com.challenge.entity.Transfer;
import com.challenge.entity.User;
import com.challenge.entity.Wallet;
import com.challenge.exception.InsufficientFundsException;
import com.challenge.repository.TransferRepository;
import com.challenge.repository.UserRepository;
import com.challenge.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock private TransferRepository transferRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    private Wallet sender;
    private Wallet recipient;

    @BeforeEach
    void setUp() {
        sender = Wallet.forUser(1L, "USD");
        ReflectionTestUtils.setField(sender, "id", 10L);
        sender.setBalanceMinor(10_000);

        recipient = Wallet.forUser(2L, "USD");
        ReflectionTestUtils.setField(recipient, "id", 20L);

        lenient().when(walletRepository.findById(10L)).thenReturn(Optional.of(sender));
        lenient().when(walletRepository.findById(20L)).thenReturn(Optional.of(recipient));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(new User("John Doe", "john@example.com")));
        lenient().when(transferRepository.save(any(Transfer.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createTransfer_movesMoneyBetweenWallets() {
        Transfer transfer = transferService.createTransfer(request("25.00"));

        assertThat(transfer.getStatus()).isEqualTo("COMPLETED");
        assertThat(sender.getBalanceMinor()).isEqualTo(7_500);
        assertThat(recipient.getBalanceMinor()).isEqualTo(2_500);
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    void createTransfer_insufficientFunds_marksTransferFailed() {
        assertThatThrownBy(() -> transferService.createTransfer(request("150.00")))
            .isInstanceOf(InsufficientFundsException.class);

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transferRepository, times(2)).save(argThat(t -> t.getStatus().equals("FAILED")));
    }

    @Test
    void createTransfer_rejectsZeroAmount() {
        assertThatThrownBy(() -> transferService.createTransfer(request("0")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createTransfer_rejectsAmountOverDailyLimit() {
        sender.setBalanceMinor(1_000_000);

        assertThatThrownBy(() -> transferService.createTransfer(request("1000.01")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static TransferRequest request(String amount) {
        return new TransferRequest(10L, 20L, new BigDecimal(amount), "lunch");
    }
}
