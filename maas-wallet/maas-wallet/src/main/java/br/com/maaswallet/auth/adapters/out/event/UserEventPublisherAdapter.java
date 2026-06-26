package br.com.maaswallet.auth.adapters.out.event;

import br.com.maaswallet.auth.ports.out.UserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisherAdapter implements UserEventPublisherPort {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishUserRegistered(String userId) {
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(userId));
    }
}
