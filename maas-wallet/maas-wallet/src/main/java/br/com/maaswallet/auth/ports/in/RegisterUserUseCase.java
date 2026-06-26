package br.com.maaswallet.auth.ports.in;

import br.com.maaswallet.auth.domain.model.User;
import br.com.maaswallet.auth.domain.model.UserType;

public interface RegisterUserUseCase {
    User register(Command command);

    record Command(
        String name,
        String document,
        String email,
        String password,
        UserType type
    ) {}
}
