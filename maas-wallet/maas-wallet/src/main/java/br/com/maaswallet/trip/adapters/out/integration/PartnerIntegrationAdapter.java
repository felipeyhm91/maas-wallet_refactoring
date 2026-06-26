package br.com.maaswallet.trip.adapters.out.integration;

import br.com.maaswallet.rewards.ports.in.CalculateCashbackUseCase;
import br.com.maaswallet.trip.domain.model.Modal;
import br.com.maaswallet.trip.domain.model.RouteOption;
import br.com.maaswallet.trip.ports.out.PartnerIntegrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PartnerIntegrationAdapter implements PartnerIntegrationPort {
    private final CalculateCashbackUseCase calculateCashbackUseCase;

    @Override
    public List<RouteOption> searchRoutes(String origin, String destination) {
        final var options = new ArrayList<RouteOption>();
        
        // Determina um fator de distância conceitual baseado nos nomes de origem e destino
        final int distFactor = Math.abs(origin.hashCode() + destination.hashCode()) % 15 + 3;

        // 1. SPTrans - Ônibus (BUS)
        final var sptransPrice = BigDecimal.valueOf(4.40);
        final var sptransCashback = calculateCashbackUseCase.calculate(
                new CalculateCashbackUseCase.Command("BUS", sptransPrice)
        );
        options.add(RouteOption.builder()
                .modal(Modal.BUS)
                .partnerId("p1-sptrans-uuid-000000000001")
                .partnerName("SPTrans")
                .price(sptransPrice)
                .durationMinutes(distFactor * 4)
                .cashbackAmount(sptransCashback)
                .description("Linha de ônibus direta com integração SPTrans")
                .build());

        // 2. Uber - Carro Privado (RIDE_HAILING)
        final var uberPrice = BigDecimal.valueOf(distFactor * 2.50 + 8.00).setScale(2, RoundingMode.HALF_UP);
        final var uberCashback = calculateCashbackUseCase.calculate(
                new CalculateCashbackUseCase.Command("RIDE_HAILING", uberPrice)
        );
        options.add(RouteOption.builder()
                .modal(Modal.RIDE_HAILING)
                .partnerId("p2-uber-uuid-00000000000002")
                .partnerName("Uber")
                .price(uberPrice)
                .durationMinutes(distFactor * 2)
                .cashbackAmount(uberCashback)
                .description("Corrida individual - Categoria UberX")
                .build());

        // 3. 99 App - Táxi / Carro Privado (TAXI)
        final var app99Price = BigDecimal.valueOf(distFactor * 2.30 + 7.50).setScale(2, RoundingMode.HALF_UP);
        final var app99Cashback = calculateCashbackUseCase.calculate(
                new CalculateCashbackUseCase.Command("TAXI", app99Price)
        );
        options.add(RouteOption.builder()
                .modal(Modal.TAXI)
                .partnerId("p3-99-uuid-0000000000000003")
                .partnerName("99 App")
                .price(app99Price)
                .durationMinutes(distFactor * 2 - 1)
                .cashbackAmount(app99Cashback)
                .description("Deslocamento rápido - Categoria 99Pop")
                .build());

        // 4. Bike Sampa - Bicicleta (BIKE)
        final var bikePrice = BigDecimal.valueOf(3.00); // Tarifa fixa inicial
        final var bikeCashback = calculateCashbackUseCase.calculate(
                new CalculateCashbackUseCase.Command("BIKE", bikePrice)
        );
        options.add(RouteOption.builder()
                .modal(Modal.BIKE)
                .partnerId("p4-bikeshare-uuid-0000000004")
                .partnerName("Bike Sampa")
                .price(bikePrice)
                .durationMinutes(distFactor * 5)
                .cashbackAmount(bikeCashback)
                .description("Aluguel de bicicleta - Estação Itaú próxima")
                .build());

        // 5. Scooter GO - Patinete (SCOOTER)
        final var scooterPrice = BigDecimal.valueOf(distFactor * 1.20 + 4.00).setScale(2, RoundingMode.HALF_UP);
        final var scooterCashback = calculateCashbackUseCase.calculate(
                new CalculateCashbackUseCase.Command("SCOOTER", scooterPrice)
        );
        options.add(RouteOption.builder()
                .modal(Modal.SCOOTER)
                .partnerId("p5-scooter-uuid-00000000005")
                .partnerName("Scooter GO")
                .price(scooterPrice)
                .durationMinutes(distFactor * 3)
                .cashbackAmount(scooterCashback)
                .description("Patinete elétrico disponível no perímetro")
                .build());

        return options;
    }

    @Override
    public String createReservation(String partnerId, Modal modal, BigDecimal price, String tripId) {
        // Simula a criação de uma reserva externa no parceiro, retornando um código único
        return "EXT-" + partnerId.substring(0, 5) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void cancelReservation(String partnerId, String partnerTripId) {
        // Apenas simula o cancelamento do webhook / reserva externa com sucesso
    }
}
