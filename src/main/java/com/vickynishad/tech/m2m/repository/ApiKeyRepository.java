package com.vickynishad.tech.m2m.repository;

import com.vickynishad.tech.m2m.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyIdAndActiveTrue(String keyId);
}