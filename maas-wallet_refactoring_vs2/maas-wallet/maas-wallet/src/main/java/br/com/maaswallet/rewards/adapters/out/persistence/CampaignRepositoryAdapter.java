package br.com.maaswallet.rewards.adapters.out.persistence;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import br.com.maaswallet.rewards.ports.out.CampaignRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CampaignRepositoryAdapter implements CampaignRepositoryPort {
    private final SpringCampaignRepository springCampaignRepository;
    private final CampaignMapper campaignMapper;

    @Override
    public CashbackCampaign save(CashbackCampaign campaign) {
        final var entity = campaignMapper.toEntity(campaign);
        final var saved = springCampaignRepository.save(entity);
        return campaignMapper.toDomain(saved);
    }

    @Override
    public Optional<CashbackCampaign> findById(String id) {
        return springCampaignRepository.findById(id).map(campaignMapper::toDomain);
    }

    @Override
    public List<CashbackCampaign> findActiveCampaigns() {
        return campaignMapper.toDomainList(springCampaignRepository.findActiveCampaigns());
    }

    @Override
    public List<CashbackCampaign> findAllCampaigns() {
        return campaignMapper.toDomainList(springCampaignRepository.findAll());
    }
}
