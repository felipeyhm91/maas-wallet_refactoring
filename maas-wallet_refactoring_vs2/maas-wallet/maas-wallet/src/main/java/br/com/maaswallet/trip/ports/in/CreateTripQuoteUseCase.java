package br.com.maaswallet.trip.ports.in;

import br.com.maaswallet.trip.domain.model.Modal;
import br.com.maaswallet.trip.domain.model.Trip;
import java.math.BigDecimal;

public interface CreateTripQuoteUseCase {
    Trip quote(Command command);

    record Command(
        String userId,
        String partnerId,
        Modal modal,
        BigDecimal price,
        BigDecimal cashbackAmount,
        String origin,
        String destination
    ) {}
}
