package com.vickynishad.tech.m2m.service;

import com.vickynishad.tech.m2m.dto.ApiKeyContracts.*;
import com.vickynishad.tech.m2m.entity.ApiClient;
import com.vickynishad.tech.m2m.entity.ApiKey;
import com.vickynishad.tech.m2m.repository.ApiClientRepository;
import com.vickynishad.tech.m2m.repository.ApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    private final ApiKeyRepository keyRepository;
    private final ApiClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(ApiKeyRepository keyRepository,
                         ApiClientRepository clientRepository,
                         PasswordEncoder passwordEncoder) {
        this.keyRepository = keyRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApiClient registerClient(ProvisionClientCommand cmd) {
        log.info("Processing client registration request for clientName: '{}'", cmd.clientName());

        return clientRepository.findByClientName(cmd.clientName())
                .map(existingClient -> {
                    log.info("Client '{}' already exists with ID: {}. Returning existing record.",
                            existingClient.getClientName(), existingClient.getId());
                    return existingClient;
                })
                .orElseGet(() -> {
                    log.info("Creating new client record for clientName: '{}', email: '{}'",
                            cmd.clientName(), cmd.contactEmail());
                    ApiClient savedClient = clientRepository.save(new ApiClient(cmd.clientName(), cmd.contactEmail()));
                    log.info("Successfully provisioned client '{}' with assigned ID: {}",
                            savedClient.getClientName(), savedClient.getId());
                    return savedClient;
                });
    }

    @Transactional
    public GeneratedKeyEnvelope generateApiKey(GenerateKeyCommand cmd) {
        log.info("Initiating API key generation for client: '{}' with requested scopes: {}",
                cmd.clientName(), cmd.scopes());

        ApiClient client = clientRepository.findByClientName(cmd.clientName())
                .orElseThrow(() -> {
                    log.warn("API key generation rejected: Client '{}' not found in database", cmd.clientName());
                    return new IllegalArgumentException("Client not found: " + cmd.clientName());
                });

        // 1. Generate Public Key ID (ak_live_<16 hex chars>)
        String keyId = "ak_live_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.debug("Generated public keyId: '{}' for client: '{}'", keyId, client.getClientName());

        // 2. Generate 256-bit Cryptographic Secret
        byte[] entropy = new byte[32];
        new SecureRandom().nextBytes(entropy);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);

        // 3. Hash secret using BCrypt
        String secretHash = passwordEncoder.encode(secret);
        log.debug("Cryptographic secret successfully generated and hashed for keyId: '{}'", keyId);

        long days = (cmd.validForDays() != null && cmd.validForDays() > 0) ? cmd.validForDays() : 365;
        Instant expiresAt = Instant.now().plus(days, ChronoUnit.DAYS);

        String scopesJoined = String.join(",", cmd.scopes());
        ApiKey apiKey = new ApiKey(keyId, secretHash, scopesJoined, expiresAt, client);
        keyRepository.save(apiKey);

        log.info("Successfully generated and activated API key [keyId: '{}'] for client: '{}' (Scopes: {}, Expires: {})",
                keyId, client.getClientName(), cmd.scopes(), expiresAt);

        // Security rule: The plaintext secret and combined key are returned only in this envelope
        // and are never written to log files.
        String combinedApiKey = keyId + "." + secret;
        return new GeneratedKeyEnvelope(client.getClientName(), keyId, secret, combinedApiKey, cmd.scopes(), expiresAt);
    }
}