package br.com.maaswallet.wallet.adapters.out.persistence;

import br.com.maaswallet.wallet.domain.model.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletEntity toEntity(Wallet domain);
    Wallet toDomain(WalletEntity entity);
}
