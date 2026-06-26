package br.com.maaswallet.trip.ports.in;

import br.com.maaswallet.trip.domain.model.Trip;

public interface ConfirmTripUsageUseCase {
    Trip confirm(String partnerTripId);
}
