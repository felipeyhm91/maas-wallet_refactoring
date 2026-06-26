package br.com.maaswallet.rewards.ports.in;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import java.util.List;

public interface ListActiveCampaignsUseCase {
    List<CashbackCampaign> listActiveCampaigns();
}
