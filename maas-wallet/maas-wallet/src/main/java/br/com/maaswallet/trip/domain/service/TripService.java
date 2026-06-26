package br.com.maaswallet.trip.domain.service;

import br.com.maaswallet.trip.domain.exception.TripException;
import br.com.maaswallet.trip.domain.exception.TripNotFoundException;
import br.com.maaswallet.trip.domain.model.Modal;
import br.com.maaswallet.trip.domain.model.Partner;
import br.com.maaswallet.trip.domain.model.RouteOption;
import br.com.maaswallet.trip.domain.model.Trip;
import br.com.maaswallet.trip.domain.model.TripStatus;
import br.com.maaswallet.trip.ports.in.BookTripUseCase;
import br.com.maaswallet.trip.ports.in.CancelTripUseCase;
import br.com.maaswallet.trip.ports.in.ConfirmTripUsageUseCase;
import br.com.maaswallet.trip.ports.in.CreateTripQuoteUseCase;
import br.com.maaswallet.trip.ports.in.RegisterPartnerUseCase;
import br.com.maaswallet.trip.ports.in.SearchRoutesUseCase;
import br.com.maaswallet.trip.ports.out.PartnerIntegrationPort;
import br.com.maaswallet.trip.ports.out.TripRepositoryPort;
import br.com.maaswallet.wallet.ports.in.CreditCashbackUseCase;
import br.com.maaswallet.wallet.ports.in.DebitWalletUseCase;
import br.com.maaswallet.wallet.ports.in.RefundTransactionUseCase;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TripService implements
        SearchRoutesUseCase,
        CreateTripQuoteUseCase,
        BookTripUseCase,
        ConfirmTripUsageUseCase,
        CancelTripUseCase,
        RegisterPartnerUseCase {

    private final TripRepositoryPort tripRepositoryPort;
    private final PartnerIntegrationPort partnerIntegrationPort;
    private final DebitWalletUseCase debitWalletUseCase;
    private final RefundTransactionUseCase refundTransactionUseCase;
    private final CreditCashbackUseCase creditCashbackUseCase;

    public TripService(TripRepositoryPort tripRepositoryPort,
                       PartnerIntegrationPort partnerIntegrationPort,
                       DebitWalletUseCase debitWalletUseCase,
                       RefundTransactionUseCase refundTransactionUseCase,
                       CreditCashbackUseCase creditCashbackUseCase) {
        this.tripRepositoryPort = tripRepositoryPort;
        this.partnerIntegrationPort = partnerIntegrationPort;
        this.debitWalletUseCase = debitWalletUseCase;
        this.refundTransactionUseCase = refundTransactionUseCase;
        this.creditCashbackUseCase = creditCashbackUseCase;
    }

    @Override
    public List<RouteOption> search(SearchRoutesUseCase.Command command) {
        if (command.origin() == null || command.origin().isBlank() ||
            command.destination() == null || command.destination().isBlank()) {
            throw new TripException("Origem e destino são obrigatórios.");
        }
        return partnerIntegrationPort.searchRoutes(command.origin(), command.destination());
    }

    @Override
    public Trip quote(CreateTripQuoteUseCase.Command command) {
        final var partner = tripRepositoryPort.findPartnerById(command.partnerId())
                .orElseThrow(() -> new TripException("Parceiro não encontrado: " + command.partnerId()));

        if (!partner.isActive()) {
            throw new TripException("O parceiro selecionado está inativo.");
        }

        final var trip = Trip.builder()
                .id(UUID.randomUUID().toString())
                .userId(command.userId())
                .partnerId(command.partnerId())
                .modal(command.modal())
                .status(TripStatus.QUOTED)
                .price(command.price())
                .cashbackAmount(command.cashbackAmount())
                .origin(command.origin())
                .destination(command.destination())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return tripRepositoryPort.save(trip);
    }

    @Override
    public Trip book(String tripId, String userId) {
        final var trip = tripRepositoryPort.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Viagem não encontrada: " + tripId));

        if (!trip.getUserId().equals(userId)) {
            throw new TripException("A cotação de viagem informada pertence a outro usuário.");
        }

        if (!trip.isReservable()) {
            throw new TripException("Esta viagem não está no estado adequado para reserva. Estado atual: " + trip.getStatus());
        }

        // 1. Debita o valor total da viagem da carteira do usuário
        final var debitCommand = new DebitWalletUseCase.Command(
                userId,
                trip.getPrice(),
                tripId,
                "Débito para pagamento de viagem multimodal"
        );
        debitWalletUseCase.debit(debitCommand);

        // 2. Efetiva a reserva junto ao parceiro
        final var partnerTripId = partnerIntegrationPort.createReservation(
                trip.getPartnerId(),
                trip.getModal(),
                trip.getPrice(),
                trip.getId()
        );

        // 3. Atualiza os dados da viagem localmente
        trip.setPartnerTripId(partnerTripId);
        trip.setStatus(TripStatus.RESERVED);
        trip.setUpdatedAt(Instant.now());

        return tripRepositoryPort.save(trip);
    }

    @Override
    public Trip confirm(String partnerTripId) {
        final var trip = tripRepositoryPort.findByPartnerTripId(partnerTripId)
                .orElseThrow(() -> new TripNotFoundException("Viagem não encontrada para o código do parceiro: " + partnerTripId));

        if (TripStatus.COMPLETED.equals(trip.getStatus())) {
            // Idempotência: Se já foi confirmada, apenas retorna
            return trip;
        }

        if (!TripStatus.RESERVED.equals(trip.getStatus()) && !TripStatus.IN_PROGRESS.equals(trip.getStatus())) {
            throw new TripException("A viagem não pode ser confirmada porque está no status: " + trip.getStatus());
        }

        // 1. Atualiza status para COMPLETED
        trip.setStatus(TripStatus.COMPLETED);
        trip.setUpdatedAt(Instant.now());
        final var savedTrip = tripRepositoryPort.save(trip);

        // 2. Se houver cashback projetado, libera o saldo de cashback
        if (trip.getCashbackAmount().compareTo(BigDecimal.ZERO) > 0) {
            final var cashbackCommand = new CreditCashbackUseCase.Command(
                    trip.getUserId(),
                    trip.getCashbackAmount(),
                    trip.getId(),
                    "Cashback recebido por viagem concluída"
            );
            creditCashbackUseCase.creditCashback(cashbackCommand);
        }

        return savedTrip;
    }

    @Override
    public Trip cancel(String tripId, String userId) {
        final var trip = tripRepositoryPort.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Viagem não encontrada: " + tripId));

        if (!trip.getUserId().equals(userId)) {
            throw new TripException("Permissão negada. A viagem não pertence ao usuário logado.");
        }

        if (!trip.isCancellable()) {
            throw new TripException("Esta viagem não pode ser cancelada. Status atual: " + trip.getStatus());
        }

        // 1. Notifica cancelamento ao parceiro
        if (trip.getPartnerTripId() != null) {
            try {
                partnerIntegrationPort.cancelReservation(trip.getPartnerId(), trip.getPartnerTripId());
            } catch (Exception e) {
                // Loga erro mas prossegue com o estorno financeiro interno
            }
        }

        // 2. Efetua o estorno do valor pago de volta à carteira do usuário
        final var refundCommand = new RefundTransactionUseCase.Command(
                userId,
                tripId,
                trip.getPrice(),
                "Estorno por cancelamento de viagem multimodal"
        );
        refundTransactionUseCase.refund(refundCommand);

        // 3. Atualiza status da viagem localmente
        trip.setStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(Instant.now());

        return tripRepositoryPort.save(trip);
    }

    @Override
    public Partner registerPartner(RegisterPartnerUseCase.Command command) {
        final var partner = Partner.builder()
                .id(UUID.randomUUID().toString())
                .name(command.name())
                .apiKey(command.apiKey())
                .status("ACTIVE")
                .build();
        return tripRepositoryPort.savePartner(partner);
    }
}
