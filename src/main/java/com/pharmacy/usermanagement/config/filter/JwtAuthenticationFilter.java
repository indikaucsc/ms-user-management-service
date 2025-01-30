package com.pharmacy.usermanagement.config.filter;

import com.pharmacy.usermanagement.service.SecuriUserDetiles;
import com.pharmacy.usermanagement.util.JwtTokenUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements Filter {

    private final JwtTokenUtil jwtTokenUtil;
    private final SecuriUserDetiles userDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, SecuriUserDetiles userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(
            jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String token = getTokenFromHeader(httpRequest);
        System.out.println("Filter token: " + token);

        try {
            if (StringUtils.hasText(token)) {
                if (!jwtTokenUtil.validateToken(token)) {
                    throw new SecurityException("Invalid JWT Token");
                }

                String username = jwtTokenUtil.getUsernameFromToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (SecurityException ex) {
            // Log the error and send an unauthorized response
//            System.err.println("Authentication failed: " + ex.getMessage());
//            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + ex.getMessage());
//            return; // Stop further execution of the filter chain
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("Received Authorization Header: " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
