package com.vickynishad.tech.m2m.repository;

import com.vickynishad.tech.m2m.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {
    Optional<ApiClient> findByClientName(String clientName);
}