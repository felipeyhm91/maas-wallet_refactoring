package br.com.maaswallet.trip.ports.in;

import br.com.maaswallet.trip.domain.model.Partner;

public interface RegisterPartnerUseCase {
    Partner registerPartner(Command command);

    record Command(
        String name,
        String apiKey
    ) {}
}
