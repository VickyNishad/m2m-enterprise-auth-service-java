package com.vickynishad.tech.m2m.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_id", nullable = false, unique = true, length = 32)
    private String keyId; // Public identifier: ak_live_xxxx

    @Column(name = "secret_hash", nullable = false)
    private String secretHash;

    @Column(nullable = false)
    private String scopes; // e.g. "orders:read,orders:write"

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private ApiClient client;

    public ApiKey() {}

    public ApiKey(String keyId, String secretHash, String scopes, Instant expiresAt, ApiClient client) {
        this.keyId = keyId;
        this.secretHash = secretHash;
        this.scopes = scopes;
        this.expiresAt = expiresAt;
        this.client = client;
        this.active = true;
    }

    public List<String> getScopeList() {
        if (scopes == null || scopes.isBlank()) return List.of();
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .toList();
    }

    public Long getId() { return id; }
    public String getKeyId() { return keyId; }
    public String getSecretHash() { return secretHash; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getExpiresAt() { return expiresAt; }
    public ApiClient getClient() { return client; }
}