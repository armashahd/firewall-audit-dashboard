package com.ntg.securityaudit.controller;

import com.ntg.securityaudit.entity.AppUser;
import com.ntg.securityaudit.enums.UserRole;
import com.ntg.securityaudit.service.ActivityLogService;
import com.ntg.securityaudit.service.AppUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('SUPERADMIN')")
public class UserManagementController {

    private final AppUserService appUserService;
    private final ActivityLogService activityLogService;

    public UserManagementController(AppUserService appUserService,
                                    ActivityLogService activityLogService) {
        this.appUserService = appUserService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", appUserService.getAllUsers());
        return "users";
    }

    @GetMapping("/users/new")
    public String showAddUserForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("formTitle", "Add User");
        model.addAttribute("submitLabel", "Save User");
        return "user-form";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        AppUser user = appUserService.getUserById(id);
        if (user == null) {
            return "redirect:/users";
        }
        model.addAttribute("appUser", user);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("formTitle", "Edit User");
        model.addAttribute("submitLabel", "Update User");
        return "user-form";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute AppUser appUser,
                           @RequestParam(required = false) String rawPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!isValidUser(appUser, rawPassword, model)) {
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("formTitle", appUser.getId() == null ? "Add User" : "Edit User");
            model.addAttribute("submitLabel", appUser.getId() == null ? "Save User" : "Update User");
            return "user-form";
        }

        AppUser existingUser = appUser.getId() != null ? appUserService.getUserById(appUser.getId()) : null;
        String oldUserSnapshot = existingUser != null ? userSnapshot(existingUser) : null;
        AppUser savedUser = appUserService.saveUser(appUser, rawPassword);
        activityLogService.log(
                existingUser == null ? "USER_CREATED" : "USER_EDITED",
                "User",
                savedUser.getId(),
                savedUser.getUsername(),
                oldUserSnapshot,
                userSnapshot(savedUser),
                null
        );
        redirectAttributes.addFlashAttribute("successMessage", "User saved.");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String rawPassword,
                                RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(rawPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password is required.");
            return "redirect:/users";
        }
        appUserService.resetPassword(id, rawPassword);
        AppUser user = appUserService.getUserById(id);
        activityLogService.log("PASSWORD_RESET", "User", id, user != null ? user.getUsername() : null, null, "Password updated", null);
        redirectAttributes.addFlashAttribute("successMessage", "Password updated.");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appUserService.setEnabled(id, true);
        AppUser user = appUserService.getUserById(id);
        activityLogService.log("USER_ENABLED", "User", id, user != null ? user.getUsername() : null, "Disabled", "Enabled", null);
        redirectAttributes.addFlashAttribute("successMessage", "User enabled.");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appUserService.setEnabled(id, false);
        AppUser user = appUserService.getUserById(id);
        activityLogService.log("USER_DISABLED", "User", id, user != null ? user.getUsername() : null, "Enabled", "Disabled", null);
        redirectAttributes.addFlashAttribute("successMessage", "User disabled.");
        return "redirect:/users";
    }

    private String userSnapshot(AppUser user) {
        return user.getDisplayName() + " / " + user.getRole() + " / " + (user.isEnabled() ? "Enabled" : "Disabled");
    }

    private boolean isValidUser(AppUser appUser, String rawPassword, Model model) {
        boolean valid = true;

        if (!StringUtils.hasText(appUser.getUsername())) {
            model.addAttribute("usernameError", "Username is required.");
            valid = false;
        } else if (appUser.getId() == null && appUserService.usernameExists(appUser.getUsername())) {
            model.addAttribute("usernameError", "Username already exists.");
            valid = false;
        }

        if (!StringUtils.hasText(appUser.getDisplayName())) {
            model.addAttribute("displayNameError", "Display name is required.");
            valid = false;
        }

        if (appUser.getRole() == null) {
            model.addAttribute("roleError", "Role is required.");
            valid = false;
        }

        if (appUser.getId() == null && !StringUtils.hasText(rawPassword)) {
            model.addAttribute("passwordError", "Password is required.");
            valid = false;
        }

        if (!valid) {
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
        }
        return valid;
    }
}
