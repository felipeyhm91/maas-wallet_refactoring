package br.com.maaswallet.wallet.ports.in;

import br.com.maaswallet.wallet.domain.model.Wallet;
import java.math.BigDecimal;

public interface DebitWalletUseCase {
    Wallet debit(Command command);

    record Command(
        String userId,
        BigDecimal amount,
        String referenceId,
        String description
    ) {}
}
