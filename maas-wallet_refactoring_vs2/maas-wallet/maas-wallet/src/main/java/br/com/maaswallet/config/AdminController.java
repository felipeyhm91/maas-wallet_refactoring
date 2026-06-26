package br.com.maaswallet.config;

import br.com.maaswallet.rewards.ports.in.CreateCampaignUseCase;
import br.com.maaswallet.trip.ports.in.RegisterPartnerUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RegisterPartnerUseCase registerPartnerUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;

    @PostMapping("/partners")
    public ResponseEntity<?> registerPartner(@Valid @RequestBody PartnerRequest request) {
        final var command = new RegisterPartnerUseCase.Command(request.name(), request.apiKey());
        final var partner = registerPartnerUseCase.registerPartner(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(partner);
    }

    @PostMapping("/campaigns")
    public ResponseEntity<?> createCampaign(@Valid @RequestBody CampaignRequest request) {
        final var command = new CreateCampaignUseCase.Command(
                request.name(),
                request.percentage(),
                request.startDate(),
                request.endDate(),
                request.modalEligible(),
                request.userLimit(),
                request.campaignLimit()
        );
        final var campaign = createCampaignUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
    }

    // DTOs
    public record PartnerRequest(
        @NotBlank(message = "Nome do parceiro é obrigatório") String name,
        @NotBlank(message = "Chave de API é obrigatória") String apiKey
    ) {}

    public record CampaignRequest(
        @NotBlank(message = "Nome da campanha é obrigatório") String name,
        @NotNull(message = "Percentual é obrigatório")
        @DecimalMin(value = "0.01", message = "Percentual mínimo é 0.01%")
        @DecimalMax(value = "100.00", message = "Percentual máximo é 100%")
        BigDecimal percentage,
        
        @NotNull(message = "Data de início é obrigatória") Instant startDate,
        @NotNull(message = "Data de término é obrigatória") Instant endDate,
        @NotBlank(message = "Modal elegível é obrigatório") String modalEligible,
        @NotNull(message = "Teto por usuário é obrigatório") BigDecimal userLimit,
        @NotNull(message = "Teto total da campanha é obrigatório") BigDecimal campaignLimit
    ) {}
}
