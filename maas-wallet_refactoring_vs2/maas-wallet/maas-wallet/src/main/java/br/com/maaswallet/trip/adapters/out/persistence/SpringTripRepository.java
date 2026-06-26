package br.com.maaswallet.trip.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

interface SpringTripRepository extends JpaRepository<TripEntity, String> {
    Optional<TripEntity> findByPartnerTripId(String partnerTripId);
    List<TripEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
