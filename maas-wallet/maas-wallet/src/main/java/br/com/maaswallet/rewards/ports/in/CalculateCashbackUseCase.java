package br.com.maaswallet.rewards.ports.in;

import java.math.BigDecimal;

public interface CalculateCashbackUseCase {
    BigDecimal calculate(Command command);

    record Command(
        String modal,
        BigDecimal price
    ) {}
}
