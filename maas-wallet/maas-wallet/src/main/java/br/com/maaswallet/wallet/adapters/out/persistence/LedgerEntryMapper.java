package br.com.maaswallet.wallet.adapters.out.persistence;

import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LedgerEntryMapper {
    LedgerEntryEntity toEntity(LedgerEntry domain);
    LedgerEntry toDomain(LedgerEntryEntity entity);
    java.util.List<LedgerEntry> toDomainList(java.util.List<LedgerEntryEntity> entities);
}
