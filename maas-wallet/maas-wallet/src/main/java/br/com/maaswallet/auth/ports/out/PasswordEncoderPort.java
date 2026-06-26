package br.com.maaswallet.auth.ports.out;

public interface PasswordEncoderPort {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
