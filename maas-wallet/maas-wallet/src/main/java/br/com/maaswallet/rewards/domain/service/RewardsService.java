package br.com.maaswallet.rewards.domain.service;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import br.com.maaswallet.rewards.ports.in.CalculateCashbackUseCase;
import br.com.maaswallet.rewards.ports.in.CreateCampaignUseCase;
import br.com.maaswallet.rewards.ports.in.ListActiveCampaignsUseCase;
import br.com.maaswallet.rewards.ports.out.CampaignRepositoryPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class RewardsService implements
        CalculateCashbackUseCase,
        ListActiveCampaignsUseCase,
        CreateCampaignUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public RewardsService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    public BigDecimal calculate(CalculateCashbackUseCase.Command command) {
        if (command.price().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        final var activeCampaigns = campaignRepositoryPort.findActiveCampaigns();
        
        // Encontra a campanha ativa elegível para o modal que oferece o maior percentual
        CashbackCampaign bestCampaign = null;
        for (final var campaign : activeCampaigns) {
            if (campaign.isActiveNow() && campaign.isEligibleFor(command.modal())) {
                if (bestCampaign == null || campaign.getPercentage().compareTo(bestCampaign.getPercentage()) > 0) {
                    bestCampaign = campaign;
                }
            }
        }

        if (bestCampaign == null) {
            return BigDecimal.ZERO;
        }

        // Calcula o valor bruto: preço * (porcentagem / 100)
        final var rate = bestCampaign.getPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        var cashback = command.price().multiply(rate).setScale(2, RoundingMode.HALF_UP);

        // Aplica o teto individual do usuário para a campanha, se aplicável
        if (bestCampaign.getUserLimit().compareTo(BigDecimal.ZERO) > 0 && 
            cashback.compareTo(bestCampaign.getUserLimit()) > 0) {
            cashback = bestCampaign.getUserLimit();
        }

        return cashback;
    }

    @Override
    public List<CashbackCampaign> listActiveCampaigns() {
        return campaignRepositoryPort.findActiveCampaigns();
    }

    @Override
    public CashbackCampaign create(CreateCampaignUseCase.Command command) {
        final var campaign = CashbackCampaign.builder()
                .id(UUID.randomUUID().toString())
                .name(command.name())
                .percentage(command.percentage())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .modalEligible(command.modalEligible())
                .userLimit(command.userLimit())
                .campaignLimit(command.campaignLimit())
                .status("ACTIVE")
                .build();
        return campaignRepositoryPort.save(campaign);
    }
}
