package br.com.maaswallet.auth.ports.in;

import br.com.maaswallet.auth.domain.model.User;

public interface GetProfileUseCase {
    User getProfile(String userId);
}
