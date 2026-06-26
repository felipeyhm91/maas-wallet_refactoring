package br.com.maaswallet.wallet.adapters.out.persistence;

import br.com.maaswallet.wallet.domain.model.Wallet;
import br.com.maaswallet.wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepositoryPort {
    private final SpringWalletRepository springWalletRepository;
    private final WalletMapper walletMapper;

    @Override
    public Wallet save(Wallet wallet) {
        final var entity = walletMapper.toEntity(wallet);
        final var saved = springWalletRepository.save(entity);
        return walletMapper.toDomain(saved);
    }

    @Override
    public Optional<Wallet> findByUserId(String userId) {
        return springWalletRepository.findByUserId(userId).map(walletMapper::toDomain);
    }

    @Override
    public Optional<Wallet> findById(String id) {
        return springWalletRepository.findById(id).map(walletMapper::toDomain);
    }
}
