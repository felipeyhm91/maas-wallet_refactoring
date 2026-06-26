package br.com.maaswallet.auth.adapters.in.web;

import br.com.maaswallet.auth.domain.model.User;
import br.com.maaswallet.auth.domain.model.UserType;
import br.com.maaswallet.auth.ports.in.AuthenticateUserUseCase;
import br.com.maaswallet.auth.ports.in.GetProfileUseCase;
import br.com.maaswallet.auth.ports.in.RegisterUserUseCase;
import br.com.maaswallet.config.JwtUtils;
import br.com.maaswallet.config.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final JwtUtils jwtUtils;

    @PostMapping("/auth/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        final var command = new RegisterUserUseCase.Command(
                request.name(),
                request.document(),
                request.email(),
                request.password(),
                request.type()
        );
        final var user = registerUserUseCase.register(command);
        final var token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getType().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(token, toResponse(user)));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        final var command = new AuthenticateUserUseCase.Command(request.email(), request.password());
        final var user = authenticateUserUseCase.authenticate(command);
        final var token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getType().name());
        return ResponseEntity.ok(new LoginResponse(token, toResponse(user)));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> me() {
        final var userId = SecurityUtils.getCurrentUserId();
        final var user = getProfileUseCase.getProfile(userId);
        return ResponseEntity.ok(toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getDocument(),
                user.getEmail(),
                user.getType().name(),
                user.getStatus().name()
        );
    }

    // DTOs de Entrada e Saída
    public record RegisterRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Documento é obrigatório") String document,
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "Senha é obrigatória") String password,
        @NotNull(message = "Tipo de usuário é obrigatório") UserType type
    ) {}

    public record LoginRequest(
        @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "Senha é obrigatória") String password
    ) {}

    public record UserResponse(
        String id,
        String name,
        String document,
        String email,
        String type,
        String status
    ) {}

    public record LoginResponse(
        String token,
        UserResponse user
    ) {}
}
