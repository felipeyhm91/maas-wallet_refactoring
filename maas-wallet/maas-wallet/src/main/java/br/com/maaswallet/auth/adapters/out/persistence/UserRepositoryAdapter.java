package br.com.maaswallet.auth.adapters.out.persistence;

import br.com.maaswallet.auth.domain.model.User;
import br.com.maaswallet.auth.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final SpringUserRepository springUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        final var entity = userMapper.toEntity(user);
        final var saved = springUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return springUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByDocument(String document) {
        return springUserRepository.findByDocument(document).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByDocument(String document) {
        return springUserRepository.existsByDocument(document);
    }
}
