package br.com.maaswallet.wallet.ports.out;

import br.com.maaswallet.wallet.domain.model.Wallet;
import java.util.Optional;

public interface WalletRepositoryPort {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByUserId(String userId);
    Optional<Wallet> findById(String id);
}
