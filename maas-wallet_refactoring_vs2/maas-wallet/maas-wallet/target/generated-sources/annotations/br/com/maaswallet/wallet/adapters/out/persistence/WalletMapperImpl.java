package br.com.maaswallet.wallet.adapters.out.persistence;

import br.com.maaswallet.wallet.domain.model.Wallet;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-25T10:46:40-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class WalletMapperImpl implements WalletMapper {

    @Override
    public WalletEntity toEntity(Wallet domain) {
        if ( domain == null ) {
            return null;
        }

        WalletEntity.WalletEntityBuilder walletEntity = WalletEntity.builder();

        walletEntity.id( domain.getId() );
        walletEntity.userId( domain.getUserId() );
        walletEntity.balance( domain.getBalance() );
        walletEntity.cashback( domain.getCashback() );
        walletEntity.status( domain.getStatus() );

        return walletEntity.build();
    }

    @Override
    public Wallet toDomain(WalletEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Wallet.WalletBuilder wallet = Wallet.builder();

        wallet.id( entity.getId() );
        wallet.userId( entity.getUserId() );
        wallet.balance( entity.getBalance() );
        wallet.cashback( entity.getCashback() );
        wallet.status( entity.getStatus() );

        return wallet.build();
    }
}
