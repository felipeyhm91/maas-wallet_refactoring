package br.com.maaswallet.trip.domain.model;

import java.math.BigDecimal;
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
public class RouteOption {
    private Modal modal;
    private String partnerId;
    private String partnerName;
    private BigDecimal price;
    private Integer durationMinutes;
    private BigDecimal cashbackAmount;
    private String description;
}
