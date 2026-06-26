package br.com.maaswallet.rewards.ports.out;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import java.util.List;
import java.util.Optional;

public interface CampaignRepositoryPort {
    CashbackCampaign save(CashbackCampaign campaign);
    Optional<CashbackCampaign> findById(String id);
    List<CashbackCampaign> findActiveCampaigns();
    List<CashbackCampaign> findAllCampaigns();
}
