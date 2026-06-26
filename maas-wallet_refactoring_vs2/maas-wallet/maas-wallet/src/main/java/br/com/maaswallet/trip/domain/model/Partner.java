package br.com.maaswallet.trip.domain.model;

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
public class Partner {
    private String id;
    private String name;
    private String status;
    private String apiKey;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }
}
