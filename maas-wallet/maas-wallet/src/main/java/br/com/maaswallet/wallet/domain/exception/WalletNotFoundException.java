package br.com.maaswallet.wallet.domain.exception;

public class WalletNotFoundException extends WalletException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}
