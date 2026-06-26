package br.com.maaswallet.rewards.adapters.in.web;

import br.com.maaswallet.rewards.domain.model.CashbackCampaign;
import br.com.maaswallet.rewards.ports.in.ListActiveCampaignsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardsController {
    private final ListActiveCampaignsUseCase listActiveCampaignsUseCase;

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getCampaigns() {
        final var campaigns = listActiveCampaignsUseCase.listActiveCampaigns();
        final var responses = campaigns.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private CampaignResponse toResponse(CashbackCampaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getPercentage(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getModalEligible(),
                campaign.getUserLimit(),
                campaign.getCampaignLimit(),
                campaign.getStatus()
        );
    }

    public record CampaignResponse(
        String id,
        String name,
        BigDecimal percentage,
        Instant startDate,
        Instant endDate,
        String modalEligible,
        BigDecimal userLimit,
        BigDecimal campaignLimit,
        String status
    ) {}
}
