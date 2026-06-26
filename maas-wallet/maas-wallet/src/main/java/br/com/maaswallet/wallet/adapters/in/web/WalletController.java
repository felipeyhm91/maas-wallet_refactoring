package br.com.maaswallet.wallet.adapters.in.web;

import br.com.maaswallet.config.SecurityUtils;
import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import br.com.maaswallet.wallet.domain.model.Wallet;
import br.com.maaswallet.wallet.ports.in.GetTransactionsUseCase;
import br.com.maaswallet.wallet.ports.in.GetWalletBalanceUseCase;
import br.com.maaswallet.wallet.ports.in.RechargeWalletUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
    private final RechargeWalletUseCase rechargeWalletUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;

    @GetMapping
    public ResponseEntity<WalletResponse> getBalance() {
        final var userId = SecurityUtils.getCurrentUserId();
        final var wallet = getWalletBalanceUseCase.getBalance(userId);
        return ResponseEntity.ok(toResponse(wallet));
    }

    @PostMapping("/recharge")
    public ResponseEntity<WalletResponse> recharge(@Valid @RequestBody RechargeRequest request) {
        final var userId = SecurityUtils.getCurrentUserId();
        final var command = new RechargeWalletUseCase.Command(
                userId,
                request.amount(),
                request.description() != null && !request.description().isBlank() 
                        ? request.description() 
                        : "Recarga de créditos via PIX"
        );
        final var wallet = rechargeWalletUseCase.recharge(command);
        return ResponseEntity.ok(toResponse(wallet));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions() {
        final var userId = SecurityUtils.getCurrentUserId();
        final var transactions = getTransactionsUseCase.getTransactions(userId);
        final var responses = transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCashback(),
                wallet.getTotalBalance(),
                wallet.getStatus().name()
        );
    }

    private TransactionResponse toResponse(LedgerEntry entry) {
        return new TransactionResponse(
                entry.getId(),
                entry.getAmount(),
                entry.getType().name(),
                entry.getDescription(),
                entry.getCreatedAt(),
                entry.getReferenceId()
        );
    }

    // DTOs
    public record RechargeRequest(
        @NotNull(message = "Valor da recarga é obrigatório")
        @DecimalMin(value = "1.00", message = "O valor mínimo de recarga é R$ 1,00")
        BigDecimal amount,
        
        String description
    ) {}

    public record WalletResponse(
        String id,
        BigDecimal balance,
        BigDecimal cashback,
        BigDecimal totalBalance,
        String status
    ) {}

    public record TransactionResponse(
        String id,
        BigDecimal amount,
        String type,
        String description,
        Instant createdAt,
        String referenceId
    ) {}
}
