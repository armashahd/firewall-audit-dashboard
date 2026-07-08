package com.ntg.securityaudit.service;

import com.ntg.securityaudit.entity.AppUser;
import com.ntg.securityaudit.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public AppUser getUserById(Long id) {
        return appUserRepository.findById(id).orElse(null);
    }

    public boolean usernameExists(String username) {
        return appUserRepository.existsByUsernameIgnoreCase(username);
    }

    @Transactional
    public AppUser saveUser(AppUser user, String rawPassword) {
        AppUser existingUser = user.getId() != null ? appUserRepository.findById(user.getId()).orElse(null) : null;
        if (existingUser != null) {
            existingUser.setUsername(user.getUsername());
            existingUser.setDisplayName(user.getDisplayName());
            existingUser.setRole(user.getRole());
            existingUser.setEnabled(user.isEnabled());
            if (StringUtils.hasText(rawPassword)) {
                existingUser.setPassword(passwordEncoder.encode(rawPassword));
            }
            return appUserRepository.save(existingUser);
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        return appUserRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String rawPassword) {
        AppUser user = appUserRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(rawPassword));
        appUserRepository.save(user);
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        AppUser user = appUserRepository.findById(id).orElseThrow();
        user.setEnabled(enabled);
        appUserRepository.save(user);
    }
}
