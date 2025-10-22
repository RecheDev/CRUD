package com.portfolio.usermanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter to prevent brute force attacks and API abuse.
 * Implements a token bucket algorithm per client IP.
 *
 * Now uses database persistence instead of in-memory storage for better reliability
 * and persistence across application restarts.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        // Identify client by IP address (handles proxy forwarding)
        String clientKey = getClientKey(request);

        // Check rate limit using the service
        RateLimitService.RateLimitResult result = rateLimitService.checkRateLimit(clientKey);

        if (!result.isAllowed()) {
            logger.warn("Rate limit exceeded for client: {}", clientKey);

            // Return 429 Too Many Requests with retry information
            response.setStatus(429);
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60"); // Seconds until reset
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            response.setContentType("application/json");
            return;
        }

        // Add rate limit information headers for client tracking
        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingTokens()));

        filterChain.doFilter(request, response);
    }

    private String getClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
