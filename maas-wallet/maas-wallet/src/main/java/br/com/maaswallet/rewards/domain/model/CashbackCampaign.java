package br.com.maaswallet.rewards.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashbackCampaign {
    private String id;
    private String name;
    private BigDecimal percentage;
    private Instant startDate;
    private Instant endDate;
    private String modalEligible;
    private BigDecimal userLimit;
    private BigDecimal campaignLimit;
    private String status;

    public boolean isActiveNow() {
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            return false;
        }
        final var now = Instant.now();
        return !now.isBefore(this.startDate) && !now.isAfter(this.endDate);
    }

    public boolean isEligibleFor(String modal) {
        return this.modalEligible.equalsIgnoreCase(modal);
    }
}
