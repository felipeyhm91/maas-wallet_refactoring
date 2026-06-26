package br.com.maaswallet.auth.ports.out;

public interface UserEventPublisherPort {
    void publishUserRegistered(String userId);
}
