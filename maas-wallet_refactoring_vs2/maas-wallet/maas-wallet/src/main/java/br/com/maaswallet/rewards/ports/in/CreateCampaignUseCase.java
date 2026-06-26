package br.com.maaswallet.rewards.ports.in;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import java.math.BigDecimal;
import java.time.Instant;

public interface CreateCampaignUseCase {
    CashbackCampaign create(Command command);

    record Command(
        String name,
        BigDecimal percentage,
        Instant startDate,
        Instant endDate,
        String modalEligible,
        BigDecimal userLimit,
        BigDecimal campaignLimit
    ) {}
}
