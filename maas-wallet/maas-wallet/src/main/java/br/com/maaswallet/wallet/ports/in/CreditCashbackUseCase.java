package br.com.maaswallet.wallet.ports.in;

import br.com.maaswallet.wallet.domain.model.Wallet;
import java.math.BigDecimal;

public interface CreditCashbackUseCase {
    Wallet creditCashback(Command command);

    record Command(
        String userId,
        BigDecimal amount,
        String referenceId,
        String description
    ) {}
}
