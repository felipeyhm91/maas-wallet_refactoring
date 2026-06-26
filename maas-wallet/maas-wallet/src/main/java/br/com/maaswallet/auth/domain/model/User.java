package br.com.maaswallet.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String document;
    private String email;
    private String password;
    private UserType type;
    private UserStatus status;

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
}
