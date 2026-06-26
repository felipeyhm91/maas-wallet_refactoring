package br.com.maaswallet.wallet.ports.in;

import br.com.maaswallet.wallet.domain.model.Wallet;

public interface InitializeWalletUseCase {
    Wallet initialize(String userId);
}
