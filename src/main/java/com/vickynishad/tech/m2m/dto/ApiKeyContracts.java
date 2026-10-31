package com.vickynishad.tech.m2m.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

public final class ApiKeyContracts {

    public record ProvisionClientCommand(
            @NotBlank String clientName,
            @NotBlank @Email String contactEmail
    ) {
        public ProvisionClientCommand {
            clientName = (clientName != null) ? clientName.trim() : null;
            contactEmail = (contactEmail != null) ? contactEmail.trim().toLowerCase() : null;
        }
    }

    public record GenerateKeyCommand(
            @NotBlank String clientName,
            @NotEmpty List<String> scopes,
            Long validForDays
    ) {}

    public record GeneratedKeyEnvelope(
            String clientName,
            String keyId,
            String secret,         // Displayed ONLY once!
            String combinedApiKey, // Format: ak_live_xyz.secret_123
            List<String> scopes,
            Instant expiresAt,
            String warning
    ) {
        public GeneratedKeyEnvelope(String clientName, String keyId, String secret, String combinedApiKey, List<String> scopes, Instant expiresAt) {
            this(clientName, keyId, secret, combinedApiKey, scopes, expiresAt, 
                 "Store this secret securely. It will NEVER be shown again.");
        }
    }
}