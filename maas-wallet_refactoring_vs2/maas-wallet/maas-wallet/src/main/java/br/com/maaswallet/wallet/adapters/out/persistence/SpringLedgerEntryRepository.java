package br.com.maaswallet.wallet.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface SpringLedgerEntryRepository extends JpaRepository<LedgerEntryEntity, String> {
    List<LedgerEntryEntity> findByWalletIdOrderByCreatedAtDesc(String walletId);
}
