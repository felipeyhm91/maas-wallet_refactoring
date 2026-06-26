package br.com.maaswallet.rewards.adapters.out.persistence;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CampaignMapper {
    CampaignEntity toEntity(CashbackCampaign domain);
    CashbackCampaign toDomain(CampaignEntity entity);
    java.util.List<CashbackCampaign> toDomainList(java.util.List<CampaignEntity> entities);
}
