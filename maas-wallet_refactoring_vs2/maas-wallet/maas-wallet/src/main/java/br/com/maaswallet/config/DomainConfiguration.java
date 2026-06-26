package br.com.maaswallet.config;

import br.com.maaswallet.auth.domain.service.AuthService;
import br.com.maaswallet.auth.ports.out.PasswordEncoderPort;
import br.com.maaswallet.auth.ports.out.UserEventPublisherPort;
import br.com.maaswallet.auth.ports.out.UserRepositoryPort;
import br.com.maaswallet.rewards.domain.service.RewardsService;
import br.com.maaswallet.rewards.ports.out.CampaignRepositoryPort;
import br.com.maaswallet.trip.domain.service.TripService;
import br.com.maaswallet.trip.ports.out.PartnerIntegrationPort;
import br.com.maaswallet.trip.ports.out.TripRepositoryPort;
import br.com.maaswallet.wallet.domain.service.WalletService;
import br.com.maaswallet.wallet.ports.in.CreditCashbackUseCase;
import br.com.maaswallet.wallet.ports.in.DebitWalletUseCase;
import br.com.maaswallet.wallet.ports.in.RefundTransactionUseCase;
import br.com.maaswallet.wallet.ports.out.LedgerRepositoryPort;
import br.com.maaswallet.wallet.ports.out.WalletRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public AuthService authService(UserRepositoryPort userRepositoryPort,
                                   PasswordEncoderPort passwordEncoderPort,
                                   UserEventPublisherPort userEventPublisherPort) {
        return new AuthService(userRepositoryPort, passwordEncoderPort, userEventPublisherPort);
    }

    @Bean
    public WalletService walletService(WalletRepositoryPort walletRepositoryPort,
                                       LedgerRepositoryPort ledgerRepositoryPort) {
        return new WalletService(walletRepositoryPort, ledgerRepositoryPort);
    }

    @Bean
    public TripService tripService(TripRepositoryPort tripRepositoryPort,
                                   PartnerIntegrationPort partnerIntegrationPort,
                                   DebitWalletUseCase debitWalletUseCase,
                                   RefundTransactionUseCase refundTransactionUseCase,
                                   CreditCashbackUseCase creditCashbackUseCase) {
        return new TripService(tripRepositoryPort, partnerIntegrationPort, debitWalletUseCase, refundTransactionUseCase, creditCashbackUseCase);
    }

    @Bean
    public RewardsService rewardsService(CampaignRepositoryPort campaignRepositoryPort) {
        return new RewardsService(campaignRepositoryPort);
    }
}
