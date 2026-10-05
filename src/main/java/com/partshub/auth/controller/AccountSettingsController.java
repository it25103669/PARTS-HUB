package com.partshub.auth.controller;

import com.partshub.auth.entity.UserAccount;
import com.partshub.auth.repository.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;

/**
 * Self-Service Account Settings Controller.
 * Allows any logged-in user to change their username and password.
 */
@RestController
@RequestMapping("/api/account")
public class AccountSettingsController {

    private final UserAccountRepository userAccountRepository;

    public AccountSettingsController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> payload) {
        String currentUsername = payload.get("currentUsername");
        String currentPassword = payload.get("currentPassword");
        String newUsername = payload.get("newUsername");
        String newPassword = payload.get("newPassword");

        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current username is required."));
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is required to verify changes."));
        }

        String curUserClean = currentUsername.trim().toLowerCase();
        Optional<UserAccount> opt = userAccountRepository.findByUsernameIgnoreCase(curUserClean);

        UserAccount account;
        if (opt.isPresent()) {
            account = opt.get();
            String computedHash = hashSha256(currentPassword);
            if (!computedHash.equalsIgnoreCase(account.getPasswordHash()) && !currentPassword.equals(account.getPasswordHash())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password verification failed. Please enter your correct current password."));
            }
        } else {
            // Seeded/fallback user account
            return ResponseEntity.badRequest().body(Map.of("error", "User account not found in database. Please log in with a valid account."));
        }

        // Validate new username
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            String newUsernameClean = newUsername.trim().toLowerCase();
            if (newUsernameClean.length() < 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username must be at least 3 characters long."));
            }
            if (userAccountRepository.existsByUsernameIgnoreCaseAndIdNot(newUsernameClean, account.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username '" + newUsername.trim() + "' is already in use by another account."));
            }
            account.setUsername(newUsernameClean);
        }

        // Validate new password
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters long for security compliance."));
            }
            account.setPasswordHash(hashSha256(newPassword));
        }

        userAccountRepository.save(account);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Credentials updated successfully.",
                "username", account.getUsername(),
                "role", account.getRole(),
                "name", account.getFullName()
        ));
    }

    private String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 digest failed", e);
        }
    }
}
