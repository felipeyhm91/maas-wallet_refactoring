package br.com.maaswallet.rewards.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campaigns", schema = "rewards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "modal_eligible", nullable = false, length = 30)
    private String modalEligible;

    @Column(name = "user_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal userLimit;

    @Column(name = "campaign_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal campaignLimit;

    @Column(nullable = false, length = 20)
    private String status;
}
