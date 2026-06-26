package br.com.maaswallet.wallet.ports.out;

import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import java.util.List;

public interface LedgerRepositoryPort {
    LedgerEntry save(LedgerEntry entry);
    List<LedgerEntry> findByWalletId(String walletId);
}
