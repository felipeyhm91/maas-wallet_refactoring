package br.com.maaswallet.trip.ports.in;

import br.com.maaswallet.trip.domain.model.Trip;

public interface CancelTripUseCase {
    Trip cancel(String tripId, String userId);
}
