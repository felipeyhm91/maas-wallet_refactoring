package br.com.maaswallet.wallet.ports.in;

import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import java.util.List;

public interface GetTransactionsUseCase {
    List<LedgerEntry> getTransactions(String userId);
}
