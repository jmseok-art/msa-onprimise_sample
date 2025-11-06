package com.example.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        

        String username = request.getHeader("X-Username");
        String roles = request.getHeader("X-Roles");

        if(StringUtils.hasText(username) && StringUtils.hasText(roles)) {
            List<String> roleList = List.of(roles.split(","));

            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, roleList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        chain.doFilter(request, response);
        
    }

}