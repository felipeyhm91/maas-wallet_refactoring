package br.com.maaswallet.auth.adapters.out.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final String userId;

    public UserRegisteredEvent(String userId) {
        super(userId);
        this.userId = userId;
    }
}
