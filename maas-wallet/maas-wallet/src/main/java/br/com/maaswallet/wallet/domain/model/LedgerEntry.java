package br.com.maaswallet.wallet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {
    private String id;
    private String walletId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private Instant createdAt;
    private String referenceId;
}
