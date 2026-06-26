package br.com.maaswallet.config;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static String getCurrentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new br.com.maaswallet.auth.domain.exception.AuthException("Usuário não autenticado.");
        }
        return (String) authentication.getPrincipal();
    }
}
