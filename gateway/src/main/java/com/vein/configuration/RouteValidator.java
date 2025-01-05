package com.vein.configuration;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    private static final List<String> OPEN_API_END_POINTS = List.of(
            "/auth/register",
            "/auth/token",
            "/auth/refresh-token",
            "/auth/validate",
            "/auth/confirm",
            "/search-api",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/auth/create-reset-password-key",
            "/auth/reset-password",
            "/auth/validate-reset-password-key"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_END_POINTS
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
