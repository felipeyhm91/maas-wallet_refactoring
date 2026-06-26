package br.com.maaswallet.wallet.adapters.in.event;

import br.com.maaswallet.auth.adapters.out.event.UserRegisteredEvent;
import br.com.maaswallet.wallet.ports.in.InitializeWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {
    private final InitializeWalletUseCase initializeWalletUseCase;

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        initializeWalletUseCase.initialize(event.getUserId());
    }
}
