package br.com.maaswallet.wallet.domain.service;

import br.com.maaswallet.wallet.domain.exception.InsufficientBalanceException;
import br.com.maaswallet.wallet.domain.exception.WalletException;
import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import br.com.maaswallet.wallet.domain.model.TransactionType;
import br.com.maaswallet.wallet.domain.model.Wallet;
import br.com.maaswallet.wallet.domain.model.WalletStatus;
import br.com.maaswallet.wallet.ports.in.CreditCashbackUseCase;
import br.com.maaswallet.wallet.ports.in.DebitWalletUseCase;
import br.com.maaswallet.wallet.ports.in.RechargeWalletUseCase;
import br.com.maaswallet.wallet.ports.in.RefundTransactionUseCase;
import br.com.maaswallet.wallet.ports.out.LedgerRepositoryPort;
import br.com.maaswallet.wallet.ports.out.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletServiceTest {

    private WalletRepositoryPort walletRepositoryPort;
    private LedgerRepositoryPort ledgerRepositoryPort;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepositoryPort = Mockito.mock(WalletRepositoryPort.class);
        ledgerRepositoryPort = Mockito.mock(LedgerRepositoryPort.class);
        walletService = new WalletService(walletRepositoryPort, ledgerRepositoryPort);
    }

    @Test
    void shouldInitializeWalletWhenUserRegisters() {
        // Given
        final var userId = "user-123";
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepositoryPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        final var wallet = walletService.initialize(userId);

        // Then
        assertThat(wallet).isNotNull();
        assertThat(wallet.getUserId()).isEqualTo(userId);
        assertThat(wallet.getBalance()).isZero();
        assertThat(wallet.getCashback()).isZero();
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        
        verify(ledgerRepositoryPort).save(any(LedgerEntry.class));
    }

    @Test
    void shouldRechargeWalletWhenValidAmountProvided() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(10.00))
                .cashback(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();
        
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final var command = new RechargeWalletUseCase.Command(userId, BigDecimal.valueOf(50.00), "Pix Recharge");

        // When
        final var wallet = walletService.recharge(command);

        // Then
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        
        final var ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepositoryPort).save(ledgerCaptor.capture());
        
        final var entry = ledgerCaptor.getValue();
        assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(entry.getType()).isEqualTo(TransactionType.RECHARGE);
        assertThat(entry.getDescription()).isEqualTo("Pix Recharge");
    }

    @Test
    void shouldThrowExceptionWhenRechargeAmountIsZeroOrNegative() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(10.00))
                .cashback(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();
        
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        final var command = new RechargeWalletUseCase.Command(userId, BigDecimal.valueOf(-5.00), "Invalid Pix");

        // When / Then
        assertThatThrownBy(() -> walletService.recharge(command))
                .isInstanceOf(WalletException.class)
                .hasMessageContaining("O valor da recarga deve ser maior que zero.");
    }

    @Test
    void shouldDebitUsingOnlyCashbackWhenCashbackIsSufficient() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(100.00))
                .cashback(BigDecimal.valueOf(40.00))
                .status(WalletStatus.ACTIVE)
                .build();

        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final var command = new DebitWalletUseCase.Command(userId, BigDecimal.valueOf(30.00), "trip-1", "Trip Payment");

        // When
        final var wallet = walletService.debit(command);

        // Then
        assertThat(wallet.getCashback()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(wallet.getTotalBalance()).isEqualByComparingTo(BigDecimal.valueOf(110.00));
        
        final var ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepositoryPort).save(ledgerCaptor.capture());
        
        final var entry = ledgerCaptor.getValue();
        assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-30.00));
        assertThat(entry.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(entry.getDescription()).contains("Debitado: R$ 0.00 de saldo, R$ 30.00 de cashback");
    }

    @Test
    void shouldDebitUsingBothCashbackAndRegularBalanceWhenCashbackIsInsufficient() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(100.00))
                .cashback(BigDecimal.valueOf(15.00))
                .status(WalletStatus.ACTIVE)
                .build();

        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final var command = new DebitWalletUseCase.Command(userId, BigDecimal.valueOf(40.00), "trip-1", "Trip Payment");

        // When
        final var wallet = walletService.debit(command);

        // Then
        assertThat(wallet.getCashback()).isZero();
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
        assertThat(wallet.getTotalBalance()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
        
        final var ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepositoryPort).save(ledgerCaptor.capture());
        
        final var entry = ledgerCaptor.getValue();
        assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-40.00));
        assertThat(entry.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(entry.getDescription()).contains("Debitado: R$ 25.00 de saldo, R$ 15.00 de cashback");
    }

    @Test
    void shouldThrowExceptionWhenTotalBalanceIsInsufficient() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(20.00))
                .cashback(BigDecimal.valueOf(10.00))
                .status(WalletStatus.ACTIVE)
                .build();

        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));

        final var command = new DebitWalletUseCase.Command(userId, BigDecimal.valueOf(40.00), "trip-1", "Trip Payment");

        // When / Then
        assertThatThrownBy(() -> walletService.debit(command))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Saldo insuficiente para realizar esta viagem.");
    }

    @Test
    void shouldCreditCashbackWhenTripCompleted() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(50.00))
                .cashback(BigDecimal.valueOf(5.00))
                .status(WalletStatus.ACTIVE)
                .build();

        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final var command = new CreditCashbackUseCase.Command(userId, BigDecimal.valueOf(10.00), "trip-1", "Completed Reward");

        // When
        final var wallet = walletService.creditCashback(command);

        // Then
        assertThat(wallet.getCashback()).isEqualByComparingTo(BigDecimal.valueOf(15.00));
        
        final var ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepositoryPort).save(ledgerCaptor.capture());
        
        final var entry = ledgerCaptor.getValue();
        assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(entry.getType()).isEqualTo(TransactionType.CASHBACK);
    }

    @Test
    void shouldRefundAmountToRegularBalanceWhenTripCancelled() {
        // Given
        final var userId = "user-123";
        final var initialWallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(BigDecimal.valueOf(50.00))
                .cashback(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();

        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        when(walletRepositoryPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final var command = new RefundTransactionUseCase.Command(userId, "trip-1", BigDecimal.valueOf(30.00), "Trip Cancelled Refund");

        // When
        final var wallet = walletService.refund(command);

        // Then
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(80.00));
        
        final var ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepositoryPort).save(ledgerCaptor.capture());
        
        final var entry = ledgerCaptor.getValue();
        assertThat(entry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
        assertThat(entry.getType()).isEqualTo(TransactionType.REFUND);
    }
}
