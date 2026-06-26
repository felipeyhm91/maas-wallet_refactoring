package br.com.maaswallet.wallet.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface SpringWalletRepository extends JpaRepository<WalletEntity, String> {
    Optional<WalletEntity> findByUserId(String userId);
}
