package br.com.maaswallet.rewards.adapters.out.persistence;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
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
public class CampaignMapperImpl implements CampaignMapper {

    @Override
    public CampaignEntity toEntity(CashbackCampaign domain) {
        if ( domain == null ) {
            return null;
        }

        CampaignEntity.CampaignEntityBuilder campaignEntity = CampaignEntity.builder();

        campaignEntity.id( domain.getId() );
        campaignEntity.name( domain.getName() );
        campaignEntity.percentage( domain.getPercentage() );
        campaignEntity.startDate( domain.getStartDate() );
        campaignEntity.endDate( domain.getEndDate() );
        campaignEntity.modalEligible( domain.getModalEligible() );
        campaignEntity.userLimit( domain.getUserLimit() );
        campaignEntity.campaignLimit( domain.getCampaignLimit() );
        campaignEntity.status( domain.getStatus() );

        return campaignEntity.build();
    }

    @Override
    public CashbackCampaign toDomain(CampaignEntity entity) {
        if ( entity == null ) {
            return null;
        }

        CashbackCampaign.CashbackCampaignBuilder cashbackCampaign = CashbackCampaign.builder();

        cashbackCampaign.id( entity.getId() );
        cashbackCampaign.name( entity.getName() );
        cashbackCampaign.percentage( entity.getPercentage() );
        cashbackCampaign.startDate( entity.getStartDate() );
        cashbackCampaign.endDate( entity.getEndDate() );
        cashbackCampaign.modalEligible( entity.getModalEligible() );
        cashbackCampaign.userLimit( entity.getUserLimit() );
        cashbackCampaign.campaignLimit( entity.getCampaignLimit() );
        cashbackCampaign.status( entity.getStatus() );

        return cashbackCampaign.build();
    }

    @Override
    public List<CashbackCampaign> toDomainList(List<CampaignEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<CashbackCampaign> list = new ArrayList<CashbackCampaign>( entities.size() );
        for ( CampaignEntity campaignEntity : entities ) {
            list.add( toDomain( campaignEntity ) );
        }

        return list;
    }
}
