package br.com.maaswallet.wallet.domain.service;

import br.com.maaswallet.wallet.domain.exception.InsufficientBalanceException;
import br.com.maaswallet.wallet.domain.exception.WalletException;
import br.com.maaswallet.wallet.domain.exception.WalletNotFoundException;
import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import br.com.maaswallet.wallet.domain.model.TransactionType;
import br.com.maaswallet.wallet.domain.model.Wallet;
import br.com.maaswallet.wallet.domain.model.WalletStatus;
import br.com.maaswallet.wallet.ports.in.CreditCashbackUseCase;
import br.com.maaswallet.wallet.ports.in.DebitWalletUseCase;
import br.com.maaswallet.wallet.ports.in.GetTransactionsUseCase;
import br.com.maaswallet.wallet.ports.in.GetWalletBalanceUseCase;
import br.com.maaswallet.wallet.ports.in.InitializeWalletUseCase;
import br.com.maaswallet.wallet.ports.in.RechargeWalletUseCase;
import br.com.maaswallet.wallet.ports.in.RefundTransactionUseCase;
import br.com.maaswallet.wallet.ports.out.LedgerRepositoryPort;
import br.com.maaswallet.wallet.ports.out.WalletRepositoryPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WalletService implements 
        InitializeWalletUseCase, 
        GetWalletBalanceUseCase, 
        RechargeWalletUseCase, 
        GetTransactionsUseCase, 
        DebitWalletUseCase, 
        RefundTransactionUseCase,
        CreditCashbackUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final LedgerRepositoryPort ledgerRepositoryPort;

    public WalletService(WalletRepositoryPort walletRepositoryPort, LedgerRepositoryPort ledgerRepositoryPort) {
        this.walletRepositoryPort = walletRepositoryPort;
        this.ledgerRepositoryPort = ledgerRepositoryPort;
    }

    @Override
    public Wallet initialize(String userId) {
        if (walletRepositoryPort.findByUserId(userId).isPresent()) {
            throw new WalletException("Usuário já possui uma carteira ativa.");
        }

        final var wallet = Wallet.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .cashback(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .build();

        final var savedWallet = walletRepositoryPort.save(wallet);

        final var entry = LedgerEntry.builder()
                .id(UUID.randomUUID().toString())
                .walletId(savedWallet.getId())
                .amount(BigDecimal.ZERO)
                .type(TransactionType.ADJUSTMENT)
                .description("Carteira criada com saldo zerado.")
                .createdAt(Instant.now())
                .build();
        ledgerRepositoryPort.save(entry);

        return savedWallet;
    }

    @Override
    public Wallet getBalance(String userId) {
        return walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId));
    }

    @Override
    public Wallet recharge(RechargeWalletUseCase.Command command) {
        final var wallet = walletRepositoryPort.findByUserId(command.userId())
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + command.userId()));

        wallet.validateActive();

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("O valor da recarga deve ser maior que zero.");
        }

        wallet.setBalance(wallet.getBalance().add(command.amount()));
        final var savedWallet = walletRepositoryPort.save(wallet);

        final var entry = LedgerEntry.builder()
                .id(UUID.randomUUID().toString())
                .walletId(savedWallet.getId())
                .amount(command.amount())
                .type(TransactionType.RECHARGE)
                .description(command.description())
                .createdAt(Instant.now())
                .build();
        ledgerRepositoryPort.save(entry);

        return savedWallet;
    }

    @Override
    public List<LedgerEntry> getTransactions(String userId) {
        final var wallet = walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId));

        return ledgerRepositoryPort.findByWalletId(wallet.getId());
    }

    @Override
    public Wallet debit(DebitWalletUseCase.Command command) {
        final var wallet = walletRepositoryPort.findByUserId(command.userId())
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + command.userId()));

        wallet.validateActive();

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("O valor do débito deve ser maior que zero.");
        }

        final var totalAvailable = wallet.getTotalBalance();
        if (totalAvailable.compareTo(command.amount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar esta viagem. Saldo disponível: R$ " + totalAvailable);
        }

        final var debitAmount = command.amount();
        BigDecimal cashbackDebited = BigDecimal.ZERO;
        BigDecimal balanceDebited = BigDecimal.ZERO;

        // Consome cashback primeiro
        if (wallet.getCashback().compareTo(debitAmount) >= 0) {
            cashbackDebited = debitAmount;
            wallet.setCashback(wallet.getCashback().subtract(debitAmount));
        } else {
            cashbackDebited = wallet.getCashback();
            final var remaining = debitAmount.subtract(cashbackDebited);
            balanceDebited = remaining;
            
            wallet.setCashback(BigDecimal.ZERO);
            wallet.setBalance(wallet.getBalance().subtract(remaining));
        }

        final var savedWallet = walletRepositoryPort.save(wallet);

        final var description = String.format("%s (Debitado: R$ %s de saldo, R$ %s de cashback)", 
                command.description(), balanceDebited.setScale(2).toString(), cashbackDebited.setScale(2).toString());

        final var entry = LedgerEntry.builder()
                .id(UUID.randomUUID().toString())
                .walletId(savedWallet.getId())
                .amount(debitAmount.negate()) // Lançamento negativo no Ledger
                .type(TransactionType.DEBIT)
                .description(description)
                .createdAt(Instant.now())
                .referenceId(command.referenceId())
                .build();
        ledgerRepositoryPort.save(entry);

        return savedWallet;
    }

    @Override
    public Wallet refund(RefundTransactionUseCase.Command command) {
        final var wallet = walletRepositoryPort.findByUserId(command.userId())
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + command.userId()));

        wallet.validateActive();

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("O valor do estorno deve ser maior que zero.");
        }

        wallet.setBalance(wallet.getBalance().add(command.amount()));
        final var savedWallet = walletRepositoryPort.save(wallet);

        final var entry = LedgerEntry.builder()
                .id(UUID.randomUUID().toString())
                .walletId(savedWallet.getId())
                .amount(command.amount()) // Lançamento positivo (crédito de estorno)
                .type(TransactionType.REFUND)
                .description(command.description())
                .createdAt(Instant.now())
                .referenceId(command.tripId())
                .build();
        ledgerRepositoryPort.save(entry);

        return savedWallet;
    }

    @Override
    public Wallet creditCashback(CreditCashbackUseCase.Command command) {
        final var wallet = walletRepositoryPort.findByUserId(command.userId())
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + command.userId()));

        wallet.validateActive();

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("O valor do cashback deve ser maior que zero.");
        }

        wallet.setCashback(wallet.getCashback().add(command.amount()));
        final var savedWallet = walletRepositoryPort.save(wallet);

        final var entry = LedgerEntry.builder()
                .id(UUID.randomUUID().toString())
                .walletId(savedWallet.getId())
                .amount(command.amount()) // Lançamento positivo
                .type(TransactionType.CASHBACK)
                .description(command.description())
                .createdAt(Instant.now())
                .referenceId(command.referenceId())
                .build();
        ledgerRepositoryPort.save(entry);

        return savedWallet;
    }
}
