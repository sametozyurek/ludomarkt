package com.vein.configuration;

import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

public class AuthorizationWebFilter implements WebFilter {

    private final String authValidateUrl;

    private final RouteValidator validator;
    private final RestTemplate restTemplate;

    public AuthorizationWebFilter(RouteValidator validator, RestTemplate restTemplate, String authValidateUrl) {
        this.validator = validator;
        this.restTemplate = restTemplate;
        this.authValidateUrl = authValidateUrl;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            if (validator.isSecured.test(exchange.getRequest()) && HttpMethod.OPTIONS != exchange.getRequest().getMethod()) {
                final var authorization = Objects.requireNonNull(exchange.getRequest().getHeaders().get("Authorization")).get(0);
                final var token = authorization.substring(7);
                if (!validateToken(token)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
                }
            }

            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("x-txId", UUID.randomUUID().toString())
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }
    }

    private boolean validateToken(String token) {
        HttpEntity<String> httpEntity = new HttpEntity<>(token, new HttpHeaders());
        ResponseEntity<Boolean> validateTokenResponseEntity = restTemplate.exchange(authValidateUrl, HttpMethod.POST, httpEntity, Boolean.class);
        return validateTokenResponseEntity.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(validateTokenResponseEntity.getBody());
    }
}