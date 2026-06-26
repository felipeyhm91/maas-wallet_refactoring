package br.com.maaswallet.trip.adapters.out.persistence;

import br.com.maaswallet.trip.domain.model.Trip;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripMapper {
    TripEntity toEntity(Trip domain);
    Trip toDomain(TripEntity entity);
    java.util.List<Trip> toDomainList(java.util.List<TripEntity> entities);
}
