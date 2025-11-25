package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final JwtService jwtService;

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return (String) auth.getPrincipal();
        }
        return null;
    }

    public String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .map(grantedAuth -> grantedAuth.getAuthority())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    public boolean canAccessUser(Integer userId) {
        if (isAdmin()) {
            return true;
        }
        String email = getCurrentUserEmail();
        return true;
    }
}
