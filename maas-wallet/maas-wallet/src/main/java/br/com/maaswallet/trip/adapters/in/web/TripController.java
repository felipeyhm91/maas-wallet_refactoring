package br.com.maaswallet.trip.adapters.in.web;

import br.com.maaswallet.config.SecurityUtils;
import br.com.maaswallet.trip.domain.model.Modal;
import br.com.maaswallet.trip.domain.model.RouteOption;
import br.com.maaswallet.trip.domain.model.Trip;
import br.com.maaswallet.trip.ports.in.BookTripUseCase;
import br.com.maaswallet.trip.ports.in.CancelTripUseCase;
import br.com.maaswallet.trip.ports.in.ConfirmTripUsageUseCase;
import br.com.maaswallet.trip.ports.in.CreateTripQuoteUseCase;
import br.com.maaswallet.trip.ports.in.SearchRoutesUseCase;
import br.com.maaswallet.trip.ports.out.TripRepositoryPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TripController {
    private final SearchRoutesUseCase searchRoutesUseCase;
    private final CreateTripQuoteUseCase createTripQuoteUseCase;
    private final BookTripUseCase bookTripUseCase;
    private final ConfirmTripUsageUseCase confirmTripUsageUseCase;
    private final CancelTripUseCase cancelTripUseCase;
    private final TripRepositoryPort tripRepositoryPort;

    @PostMapping("/routes/search")
    public ResponseEntity<List<RouteOptionResponse>> search(@Valid @RequestBody SearchRequest request) {
        final var command = new SearchRoutesUseCase.Command(request.origin(), request.destination());
        final var routes = searchRoutesUseCase.search(command);
        final var responses = routes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/trips/quote")
    public ResponseEntity<TripResponse> quote(@Valid @RequestBody QuoteRequest request) {
        final var userId = SecurityUtils.getCurrentUserId();
        final var command = new CreateTripQuoteUseCase.Command(
                userId,
                request.partnerId(),
                request.modal(),
                request.price(),
                request.cashbackAmount(),
                request.origin(),
                request.destination()
        );
        final var trip = createTripQuoteUseCase.quote(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(trip));
    }

    @PostMapping("/trips")
    public ResponseEntity<TripResponse> book(@Valid @RequestBody BookRequest request) {
        final var userId = SecurityUtils.getCurrentUserId();
        final var trip = bookTripUseCase.book(request.tripId(), userId);
        return ResponseEntity.ok(toResponse(trip));
    }

    @PostMapping("/trips/cancel")
    public ResponseEntity<TripResponse> cancel(@Valid @RequestBody CancelRequest request) {
        final var userId = SecurityUtils.getCurrentUserId();
        final var trip = cancelTripUseCase.cancel(request.tripId(), userId);
        return ResponseEntity.ok(toResponse(trip));
    }

    @GetMapping("/trips")
    public ResponseEntity<List<TripResponse>> listTrips() {
        final var userId = SecurityUtils.getCurrentUserId();
        final var trips = tripRepositoryPort.findByUserId(userId);
        final var responses = trips.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/partners/webhooks/trip-status")
    public ResponseEntity<TripResponse> webhook(@Valid @RequestBody WebhookRequest request) {
        if ("COMPLETED".equalsIgnoreCase(request.status())) {
            final var trip = confirmTripUsageUseCase.confirm(request.partnerTripId());
            return ResponseEntity.ok(toResponse(trip));
        }
        return ResponseEntity.badRequest().build();
    }

    private RouteOptionResponse toResponse(RouteOption option) {
        return new RouteOptionResponse(
                option.getModal().name(),
                option.getPartnerId(),
                option.getPartnerName(),
                option.getPrice(),
                option.getDurationMinutes(),
                option.getCashbackAmount(),
                option.getDescription()
        );
    }

    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getUserId(),
                trip.getPartnerId(),
                trip.getModal().name(),
                trip.getStatus().name(),
                trip.getPrice(),
                trip.getCashbackAmount(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getCreatedAt(),
                trip.getUpdatedAt(),
                trip.getPartnerTripId()
        );
    }

    // DTOs
    public record SearchRequest(
        @NotBlank(message = "Origem é obrigatória") String origin,
        @NotBlank(message = "Destino é obrigatório") String destination
    ) {}

    public record RouteOptionResponse(
        String modal,
        String partnerId,
        String partnerName,
        BigDecimal price,
        Integer durationMinutes,
        BigDecimal cashbackAmount,
        String description
    ) {}

    public record QuoteRequest(
        @NotBlank(message = "Parceiro é obrigatório") String partnerId,
        @NotNull(message = "Modal é obrigatório") Modal modal,
        @NotNull(message = "Preço é obrigatório") BigDecimal price,
        @NotNull(message = "Valor de cashback é obrigatório") BigDecimal cashbackAmount,
        @NotBlank(message = "Origem é obrigatória") String origin,
        @NotBlank(message = "Destino é obrigatório") String destination
    ) {}

    public record BookRequest(
        @NotBlank(message = "ID da viagem é obrigatório") String tripId
    ) {}

    public record CancelRequest(
        @NotBlank(message = "ID da viagem é obrigatório") String tripId
    ) {}

    public record WebhookRequest(
        @NotBlank(message = "partnerTripId é obrigatório") String partnerTripId,
        @NotBlank(message = "Status é obrigatório") String status
    ) {}

    public record TripResponse(
        String id,
        String userId,
        String partnerId,
        String modal,
        String status,
        BigDecimal price,
        BigDecimal cashbackAmount,
        String origin,
        String destination,
        Instant createdAt,
        Instant updatedAt,
        String partnerTripId
    ) {}
}
