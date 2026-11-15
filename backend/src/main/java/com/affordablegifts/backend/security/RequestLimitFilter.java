package com.affordablegifts.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Basic abuse protection, per client IP:
 * <ul>
 *   <li>placing orders is capped (each order can send an email, so this also stops
 *       the shop being used to spam someone's inbox);</li>
 *   <li>too many wrong admin keys locks admin calls out for a while (brute force);</li>
 *   <li>request bodies over 20 KB are refused (1.1 MB for admin photo uploads).</li>
 * </ul>
 * Counters live in memory, so they reset on restart and are per instance. Behind a
 * reverse proxy the client IP is the proxy's unless forwarded headers are configured
 * (see SECURITY.md).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLimitFilter extends OncePerRequestFilter {

    private static final long MAX_BODY_BYTES = 20_000;
    /** Photo uploads (admin only) get more room; the image service enforces its own limit. */
    private static final long MAX_UPLOAD_BYTES = 1_100_000;

    private final SlidingWindowLimiter orders;
    private final SlidingWindowLimiter adminFailures;

    public RequestLimitFilter(
            @Value("${app.security.orders-per-hour:10}") int ordersPerHour,
            @Value("${app.security.admin-failures-per-15-min:10}") int adminFailures) {
        this.orders = new SlidingWindowLimiter(ordersPerHour, Duration.ofHours(1));
        this.adminFailures = new SlidingWindowLimiter(adminFailures, Duration.ofMinutes(15));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String client = request.getRemoteAddr();
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean upload = "POST".equals(method) && path.equals("/api/admin/images");
        if (request.getContentLengthLong() > (upload ? MAX_UPLOAD_BYTES : MAX_BODY_BYTES)) {
            reject(response, 413, "Request too large");
            return;
        }

        if (path.startsWith("/api/admin")) {
            if (!adminFailures.isAllowed(client)) {
                reject(response, 429, "Too many failed attempts. Try again later.");
                return;
            }
            chain.doFilter(request, response);
            if (response.getStatus() == 401) {
                adminFailures.record(client);
            }
            return;
        }

        if ("POST".equals(method) && path.equals("/api/orders")) {
            if (!orders.isAllowed(client)) {
                reject(response, 429, "Too many orders from this connection. Try again later.");
                return;
            }
            orders.record(client);
        }
        chain.doFilter(request, response);
    }

    private static void reject(HttpServletResponse response, int status, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        if (status == 429) {
            response.setHeader("Retry-After", "900");
        }
        response.getWriter().write("{\"status\":" + status + ",\"detail\":\"" + detail + "\"}");
    }
}
