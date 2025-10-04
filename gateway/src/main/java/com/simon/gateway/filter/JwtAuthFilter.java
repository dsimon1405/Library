package com.simon.gateway.filter;

import com.simon.gateway.jwt.JWTUtil;
import com.simon.name.Headers;
import com.simon.name.UserRoles;
import com.simon.name.PathRoles;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JwtAuthFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.contains(PathRoles.SERVICE)) {    //  services internal communication makes without gateway
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
            return;
        }
        boolean required_admin = uri.contains(PathRoles.ADMIN);   //  required admin rites
        boolean required_user = required_admin || uri.contains(PathRoles.USER);   //  admin have user rights

        if (!required_admin && !required_user) {
            filterChain.doFilter(createWrapper(request, null), response);
            return;
        }

        String bearer = "Bearer ";
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(bearer)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        try {
            String token = header.substring(bearer.length());
            Claims claims = jwtUtil.validateToken(token);
            List<String> roles = claims.get("roles", List.class);

            boolean admin_rights = roles.contains(UserRoles.ROLE_ADMIN);
            boolean user_rights = admin_rights || roles.contains(UserRoles.ROLE_USER);
            if (required_admin && !admin_rights || !user_rights) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
                return;
            }

            filterChain.doFilter(createWrapper(request, claims.getSubject()), response);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
        }
    }

    private HttpServletRequest createWrapper(HttpServletRequest request, String userId) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (userId != null && Headers.USER_ID.equals(name)) return userId;
                if (Headers.ORIGINAL_PATH.equals(name)) return request.getRequestURI();
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (userId != null && Headers.USER_ID.equals(name)) return Collections.enumeration(List.of(userId));
                if (Headers.ORIGINAL_PATH.equals(name)) return Collections.enumeration(List.of(request.getRequestURI().toString()));
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (userId != null && !names.contains(Headers.USER_ID)) names.add(Headers.USER_ID);
                if (!names.contains(Headers.ORIGINAL_PATH)) names.add(Headers.ORIGINAL_PATH);
                return Collections.enumeration(names);
            }
        };
    }
}
