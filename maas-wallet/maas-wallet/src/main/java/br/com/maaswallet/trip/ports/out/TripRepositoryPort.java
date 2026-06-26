package br.com.maaswallet.trip.ports.out;

import br.com.maaswallet.trip.domain.model.Partner;
import br.com.maaswallet.trip.domain.model.Trip;
import java.util.List;
import java.util.Optional;

public interface TripRepositoryPort {
    Trip save(Trip trip);
    Optional<Trip> findById(String id);
    Optional<Trip> findByPartnerTripId(String partnerTripId);
    List<Trip> findByUserId(String userId);
    
    // Partner Management
    Partner savePartner(Partner partner);
    Optional<Partner> findPartnerById(String partnerId);
    List<Partner> findAllPartners();
}
