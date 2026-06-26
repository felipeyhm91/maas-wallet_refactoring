package br.com.maaswallet.wallet.adapters.out.persistence;

import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import br.com.maaswallet.wallet.ports.out.LedgerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LedgerRepositoryAdapter implements LedgerRepositoryPort {
    private final SpringLedgerEntryRepository springLedgerEntryRepository;
    private final LedgerEntryMapper ledgerEntryMapper;

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        final var entity = ledgerEntryMapper.toEntity(entry);
        final var saved = springLedgerEntryRepository.save(entity);
        return ledgerEntryMapper.toDomain(saved);
    }

    @Override
    public List<LedgerEntry> findByWalletId(String walletId) {
        final var entities = springLedgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        return ledgerEntryMapper.toDomainList(entities);
    }
}
