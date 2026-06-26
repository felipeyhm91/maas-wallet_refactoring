package br.com.maaswallet.trip.adapters.out.persistence;

import br.com.maaswallet.trip.domain.model.Trip;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-25T10:46:40-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class TripMapperImpl implements TripMapper {

    @Override
    public TripEntity toEntity(Trip domain) {
        if ( domain == null ) {
            return null;
        }

        TripEntity.TripEntityBuilder tripEntity = TripEntity.builder();

        tripEntity.id( domain.getId() );
        tripEntity.userId( domain.getUserId() );
        tripEntity.partnerId( domain.getPartnerId() );
        tripEntity.modal( domain.getModal() );
        tripEntity.status( domain.getStatus() );
        tripEntity.price( domain.getPrice() );
        tripEntity.cashbackAmount( domain.getCashbackAmount() );
        tripEntity.origin( domain.getOrigin() );
        tripEntity.destination( domain.getDestination() );
        tripEntity.createdAt( domain.getCreatedAt() );
        tripEntity.updatedAt( domain.getUpdatedAt() );
        tripEntity.partnerTripId( domain.getPartnerTripId() );

        return tripEntity.build();
    }

    @Override
    public Trip toDomain(TripEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Trip.TripBuilder trip = Trip.builder();

        trip.id( entity.getId() );
        trip.userId( entity.getUserId() );
        trip.partnerId( entity.getPartnerId() );
        trip.modal( entity.getModal() );
        trip.status( entity.getStatus() );
        trip.price( entity.getPrice() );
        trip.cashbackAmount( entity.getCashbackAmount() );
        trip.origin( entity.getOrigin() );
        trip.destination( entity.getDestination() );
        trip.createdAt( entity.getCreatedAt() );
        trip.updatedAt( entity.getUpdatedAt() );
        trip.partnerTripId( entity.getPartnerTripId() );

        return trip.build();
    }

    @Override
    public List<Trip> toDomainList(List<TripEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<Trip> list = new ArrayList<Trip>( entities.size() );
        for ( TripEntity tripEntity : entities ) {
            list.add( toDomain( tripEntity ) );
        }

        return list;
    }
}
