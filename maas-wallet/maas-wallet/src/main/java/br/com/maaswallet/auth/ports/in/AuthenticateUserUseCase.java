package br.com.maaswallet.auth.ports.in;

import br.com.maaswallet.auth.domain.model.User;

public interface AuthenticateUserUseCase {
    User authenticate(Command command);

    record Command(
        String email,
        String password
    ) {}
}
