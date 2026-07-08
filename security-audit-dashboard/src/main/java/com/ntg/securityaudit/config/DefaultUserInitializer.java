package com.ntg.securityaudit.config;

import com.ntg.securityaudit.entity.AppUser;
import com.ntg.securityaudit.enums.UserRole;
import com.ntg.securityaudit.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDefaultUser("superadmin", "Super Administrator", UserRole.SUPERADMIN, "SuperAdmin@123");
        createDefaultUser("admin", "Administrator", UserRole.ADMIN, "Admin@123");
        createDefaultUser("auditor", "Auditor", UserRole.AUDITOR, "Auditor@123");
    }

    private void createDefaultUser(String username, String displayName, UserRole role, String rawPassword) {
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        appUserRepository.save(user);
    }
}
