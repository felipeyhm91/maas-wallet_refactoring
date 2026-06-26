package br.com.maaswallet.trip.ports.out;

import br.com.maaswallet.trip.domain.model.Modal;
import br.com.maaswallet.trip.domain.model.RouteOption;
import java.math.BigDecimal;
import java.util.List;

public interface PartnerIntegrationPort {
    List<RouteOption> searchRoutes(String origin, String destination);
    String createReservation(String partnerId, Modal modal, BigDecimal price, String tripId);
    void cancelReservation(String partnerId, String partnerTripId);
}
