package br.com.maaswallet.wallet.adapters.out.persistence;

import br.com.maaswallet.wallet.domain.model.LedgerEntry;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-25T10:46:40-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class LedgerEntryMapperImpl implements LedgerEntryMapper {

    @Override
    public LedgerEntryEntity toEntity(LedgerEntry domain) {
        if ( domain == null ) {
            return null;
        }

        LedgerEntryEntity.LedgerEntryEntityBuilder ledgerEntryEntity = LedgerEntryEntity.builder();

        ledgerEntryEntity.id( domain.getId() );
        ledgerEntryEntity.walletId( domain.getWalletId() );
        ledgerEntryEntity.amount( domain.getAmount() );
        ledgerEntryEntity.type( domain.getType() );
        ledgerEntryEntity.description( domain.getDescription() );
        ledgerEntryEntity.createdAt( domain.getCreatedAt() );
        ledgerEntryEntity.referenceId( domain.getReferenceId() );

        return ledgerEntryEntity.build();
    }

    @Override
    public LedgerEntry toDomain(LedgerEntryEntity entity) {
        if ( entity == null ) {
            return null;
        }

        LedgerEntry.LedgerEntryBuilder ledgerEntry = LedgerEntry.builder();

        ledgerEntry.id( entity.getId() );
        ledgerEntry.walletId( entity.getWalletId() );
        ledgerEntry.amount( entity.getAmount() );
        ledgerEntry.type( entity.getType() );
        ledgerEntry.description( entity.getDescription() );
        ledgerEntry.createdAt( entity.getCreatedAt() );
        ledgerEntry.referenceId( entity.getReferenceId() );

        return ledgerEntry.build();
    }

    @Override
    public List<LedgerEntry> toDomainList(List<LedgerEntryEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<LedgerEntry> list = new ArrayList<LedgerEntry>( entities.size() );
        for ( LedgerEntryEntity ledgerEntryEntity : entities ) {
            list.add( toDomain( ledgerEntryEntity ) );
        }

        return list;
    }
}
