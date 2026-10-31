package com.vickynishad.tech.m2m.filter;

import com.vickynishad.tech.m2m.entity.ApiKey;
import com.vickynishad.tech.m2m.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    public static final String API_KEY_HEADER = "X-API-KEY";

    private final ApiKeyRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyAuthFilter(ApiKeyRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String headerValue = request.getHeader(API_KEY_HEADER);

        // 1. Missing or empty header
        if (headerValue == null || headerValue.isBlank()) {
            log.trace("No '{}' header found for request [{} {}]",
                    API_KEY_HEADER, request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Malformed key format (must be keyId.secret)
        if (!headerValue.contains(".")) {
            log.warn("Malformed '{}' header format encountered on [{} {}]. Expected 'keyId.secret'",
                    API_KEY_HEADER, request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String[] parts = headerValue.split("\\.", 2);
        String keyId = parts[0];
        String secret = parts[1];

        log.debug("Authenticating M2M request with keyId: '{}' on path: '{}'", keyId, request.getRequestURI());

        Optional<ApiKey> apiKeyOpt = repository.findByKeyIdAndActiveTrue(keyId);

        if (apiKeyOpt.isEmpty()) {
            log.warn("M2M Authentication failed: No active API key found for keyId: '{}'", keyId);
            filterChain.doFilter(request, response);
            return;
        }

        ApiKey apiKey = apiKeyOpt.get();

        // 3. Check expiration
        boolean isExpired = apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(Instant.now());
        if (isExpired) {
            log.warn("M2M Authentication failed: API key [keyId: '{}'] expired at {}",
                    keyId, apiKey.getExpiresAt());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Validate cryptographic secret hash
        if (!passwordEncoder.matches(secret, apiKey.getSecretHash())) {
            log.warn("M2M Authentication failed: Invalid secret provided for keyId: '{}'", keyId);
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Authentication success: establish security context
        List<SimpleGrantedAuthority> authorities = apiKey.getScopeList().stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .toList();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                apiKey.getClient().getClientName(),
                null,
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("M2M Authentication successful for client: '{}' [keyId: '{}'] with granted scopes: {}",
                apiKey.getClient().getClientName(), keyId, apiKey.getScopeList());

        filterChain.doFilter(request, response);
    }
}