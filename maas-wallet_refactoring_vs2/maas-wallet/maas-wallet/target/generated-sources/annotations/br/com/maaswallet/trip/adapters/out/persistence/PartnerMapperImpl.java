package br.com.maaswallet.trip.adapters.out.persistence;

import br.com.maaswallet.trip.domain.model.Partner;
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
public class PartnerMapperImpl implements PartnerMapper {

    @Override
    public PartnerEntity toEntity(Partner domain) {
        if ( domain == null ) {
            return null;
        }

        PartnerEntity.PartnerEntityBuilder partnerEntity = PartnerEntity.builder();

        partnerEntity.id( domain.getId() );
        partnerEntity.name( domain.getName() );
        partnerEntity.status( domain.getStatus() );
        partnerEntity.apiKey( domain.getApiKey() );

        return partnerEntity.build();
    }

    @Override
    public Partner toDomain(PartnerEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Partner.PartnerBuilder partner = Partner.builder();

        partner.id( entity.getId() );
        partner.name( entity.getName() );
        partner.status( entity.getStatus() );
        partner.apiKey( entity.getApiKey() );

        return partner.build();
    }

    @Override
    public List<Partner> toDomainList(List<PartnerEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<Partner> list = new ArrayList<Partner>( entities.size() );
        for ( PartnerEntity partnerEntity : entities ) {
            list.add( toDomain( partnerEntity ) );
        }

        return list;
    }
}
