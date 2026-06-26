package br.com.maaswallet.auth.adapters.out.persistence;

import br.com.maaswallet.auth.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(User domain);
    User toDomain(UserEntity entity);
}
