package com.vickynishad.tech.m2m.controller;

import com.vickynishad.tech.m2m.dto.ApiKeyContracts.*;
import com.vickynishad.tech.m2m.entity.ApiClient;
import com.vickynishad.tech.m2m.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ApiKeyManagementController {

    private final ApiKeyService service;

    public ApiKeyManagementController(ApiKeyService service) {
        this.service = service;
    }

    @PostMapping("/clients")
    public ResponseEntity<ApiClient> registerClient(@Valid @RequestBody ProvisionClientCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerClient(cmd));
    }

    @PostMapping("/keys/generate")
    public ResponseEntity<GeneratedKeyEnvelope> generateKey(@Valid @RequestBody GenerateKeyCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generateApiKey(cmd));
    }
}