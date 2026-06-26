package br.com.maaswallet.auth.adapters.out.persistence;

import br.com.maaswallet.auth.domain.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-25T10:46:40-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toEntity(User domain) {
        if ( domain == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.id( domain.getId() );
        userEntity.name( domain.getName() );
        userEntity.document( domain.getDocument() );
        userEntity.email( domain.getEmail() );
        userEntity.password( domain.getPassword() );
        userEntity.type( domain.getType() );
        userEntity.status( domain.getStatus() );

        return userEntity.build();
    }

    @Override
    public User toDomain(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( entity.getId() );
        user.name( entity.getName() );
        user.document( entity.getDocument() );
        user.email( entity.getEmail() );
        user.password( entity.getPassword() );
        user.type( entity.getType() );
        user.status( entity.getStatus() );

        return user.build();
    }
}
