package br.com.maaswallet.wallet.domain.model;

import br.com.maaswallet.wallet.domain.exception.WalletException;
import java.math.BigDecimal;
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
public class Wallet {
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal cashback;
    private WalletStatus status;

    public boolean isActive() {
        return WalletStatus.ACTIVE.equals(this.status);
    }

    public void validateActive() {
        if (!isActive()) {
            throw new WalletException("A carteira está bloqueada para transações.");
        }
    }

    public BigDecimal getTotalBalance() {
        return this.balance.add(this.cashback);
    }
}
