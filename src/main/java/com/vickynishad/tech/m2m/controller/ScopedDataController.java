package com.vickynishad.tech.m2m.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/data")
public class ScopedDataController {

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "client", auth.getPrincipal(),
                "grantedScopes", auth.getAuthorities(),
                "data", "Order records fetched successfully (Authorized with SCOPE_orders:read)"
        ));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "client", auth.getPrincipal(),
                "status", "Order created (Authorized with SCOPE_orders:write)"
        ));
    }
}