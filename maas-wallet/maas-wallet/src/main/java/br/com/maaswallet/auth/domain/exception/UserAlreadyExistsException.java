package br.com.maaswallet.auth.domain.exception;

public class UserAlreadyExistsException extends AuthException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
