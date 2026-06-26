package br.com.maaswallet.rewards.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

interface SpringCampaignRepository extends JpaRepository<CampaignEntity, String> {
    @Query("SELECT c FROM CampaignEntity c WHERE c.status = 'ACTIVE' AND CURRENT_TIMESTAMP BETWEEN c.startDate AND c.endDate")
    List<CampaignEntity> findActiveCampaigns();
}
