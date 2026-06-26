package br.com.maaswallet.wallet.ports.in;

import br.com.maaswallet.wallet.domain.model.Wallet;
import java.math.BigDecimal;

public interface RefundTransactionUseCase {
    Wallet refund(Command command);

    record Command(
        String userId,
        String tripId,
        BigDecimal amount,
        String description
    ) {}
}
