package br.com.maaswallet.auth.ports.out;

import br.com.maaswallet.auth.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByDocument(String document);
    boolean existsByEmail(String email);
    boolean existsByDocument(String document);
}
