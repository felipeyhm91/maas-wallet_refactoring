package br.com.maaswallet.trip.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    private String id;
    private String userId;
    private String partnerId;
    private Modal modal;
    private TripStatus status;
    private BigDecimal price;
    private BigDecimal cashbackAmount;
    private String origin;
    private String destination;
    private Instant createdAt;
    private Instant updatedAt;
    private String partnerTripId;

    public boolean isReservable() {
        return TripStatus.QUOTED.equals(this.status);
    }

    public boolean isCancellable() {
        return TripStatus.RESERVED.equals(this.status) || TripStatus.IN_PROGRESS.equals(this.status);
    }
}
