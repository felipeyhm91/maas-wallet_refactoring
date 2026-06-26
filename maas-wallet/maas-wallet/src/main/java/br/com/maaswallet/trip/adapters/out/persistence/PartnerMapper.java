package br.com.maaswallet.trip.adapters.out.persistence;

import br.com.maaswallet.trip.domain.model.Partner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartnerMapper {
    PartnerEntity toEntity(Partner domain);
    Partner toDomain(PartnerEntity entity);
    java.util.List<Partner> toDomainList(java.util.List<PartnerEntity> entities);
}
