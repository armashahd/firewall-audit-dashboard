package com.ntg.securityaudit.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityModelAdvice {

    @ModelAttribute
    public void addSecurityFlags(Model model, Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        boolean superAdmin = hasRole(authentication, "ROLE_SUPERADMIN");
        boolean admin = hasRole(authentication, "ROLE_ADMIN");
        model.addAttribute("currentUsername", authenticated ? authentication.getName() : "");
        model.addAttribute("canManageAll", superAdmin);
        model.addAttribute("canViewSites", superAdmin || admin);
        model.addAttribute("canViewActivityLogs", superAdmin || admin);
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
