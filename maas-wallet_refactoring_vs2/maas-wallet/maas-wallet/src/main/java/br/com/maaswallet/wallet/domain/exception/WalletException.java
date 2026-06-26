package br.com.maaswallet.wallet.domain.exception;

public class WalletException extends RuntimeException {
    public WalletException(String message) {
        super(message);
    }
}
