package br.com.maaswallet.trip.adapters.out.persistence;

import br.com.maaswallet.trip.domain.model.Partner;
import br.com.maaswallet.trip.domain.model.Trip;
import br.com.maaswallet.trip.ports.out.TripRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TripRepositoryAdapter implements TripRepositoryPort {
    private final SpringTripRepository springTripRepository;
    private final SpringPartnerRepository springPartnerRepository;
    private final TripMapper tripMapper;
    private final PartnerMapper partnerMapper;

    @Override
    public Trip save(Trip trip) {
        final var entity = tripMapper.toEntity(trip);
        final var saved = springTripRepository.save(entity);
        return tripMapper.toDomain(saved);
    }

    @Override
    public Optional<Trip> findById(String id) {
        return springTripRepository.findById(id).map(tripMapper::toDomain);
    }

    @Override
    public Optional<Trip> findByPartnerTripId(String partnerTripId) {
        return springTripRepository.findByPartnerTripId(partnerTripId).map(tripMapper::toDomain);
    }

    @Override
    public List<Trip> findByUserId(String userId) {
        final var entities = springTripRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tripMapper.toDomainList(entities);
    }

    @Override
    public Partner savePartner(Partner partner) {
        final var entity = partnerMapper.toEntity(partner);
        final var saved = springPartnerRepository.save(entity);
        return partnerMapper.toDomain(saved);
    }

    @Override
    public Optional<Partner> findPartnerById(String partnerId) {
        return springPartnerRepository.findById(partnerId).map(partnerMapper::toDomain);
    }

    @Override
    public List<Partner> findAllPartners() {
        return partnerMapper.toDomainList(springPartnerRepository.findAll());
    }
}
