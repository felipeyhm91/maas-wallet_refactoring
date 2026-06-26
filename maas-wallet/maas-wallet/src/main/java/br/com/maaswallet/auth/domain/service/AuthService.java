package br.com.maaswallet.auth.domain.service;

import br.com.maaswallet.auth.domain.exception.InvalidCredentialsException;
import br.com.maaswallet.auth.domain.exception.UserAlreadyExistsException;
import br.com.maaswallet.auth.domain.model.User;
import br.com.maaswallet.auth.domain.model.UserStatus;
import br.com.maaswallet.auth.ports.in.AuthenticateUserUseCase;
import br.com.maaswallet.auth.ports.in.GetProfileUseCase;
import br.com.maaswallet.auth.ports.in.RegisterUserUseCase;
import br.com.maaswallet.auth.ports.out.PasswordEncoderPort;
import br.com.maaswallet.auth.ports.out.UserEventPublisherPort;
import br.com.maaswallet.auth.ports.out.UserRepositoryPort;
import java.util.UUID;

public class AuthService implements RegisterUserUseCase, AuthenticateUserUseCase, GetProfileUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final UserEventPublisherPort userEventPublisherPort;

    public AuthService(UserRepositoryPort userRepositoryPort,
                       PasswordEncoderPort passwordEncoderPort,
                       UserEventPublisherPort userEventPublisherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.userEventPublisherPort = userEventPublisherPort;
    }

    @Override
    public User register(RegisterUserUseCase.Command command) {
        if (userRepositoryPort.existsByDocument(command.document())) {
            throw new UserAlreadyExistsException("Documento já cadastrado: " + command.document());
        }
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("E-mail já cadastrado: " + command.email());
        }

        final var user = User.builder()
                .id(UUID.randomUUID().toString())
                .name(command.name())
                .document(command.document())
                .email(command.email())
                .password(passwordEncoderPort.encode(command.password()))
                .type(command.type())
                .status(UserStatus.ACTIVE)
                .build();

        final var savedUser = userRepositoryPort.save(user);

        // Dispara o evento de criação do usuário para os demais módulos
        userEventPublisherPort.publishUserRegistered(savedUser.getId());

        return savedUser;
    }

    @Override
    public User authenticate(AuthenticateUserUseCase.Command command) {
        final var user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas."));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Conta de usuário inativa ou bloqueada.");
        }

        if (!passwordEncoderPort.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        return user;
    }

    @Override
    public User getProfile(String userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário não encontrado: " + userId));
    }
}
