package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import com.example.apigateway.filter.JwtAuthenticationWebFilter;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
    
    private final ServerAuthenticationEntryPoint authenticationEntryPoint;
    private final ServerAccessDeniedHandler accessDeniedHandler;
    private final String publicKey;

    public SecurityConfig(@Value("${PUBLIC_KEY}") String publicKey,
        ServerAuthenticationEntryPoint authenticationEntryPoint,
        ServerAccessDeniedHandler accessDeniedHandler) {

        log.info("Public Key: {}", publicKey);

        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.publicKey = publicKey;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .exceptionHandling(handler -> handler
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/login", "/refresh").permitAll()
                .pathMatchers(HttpMethod.POST, "/users").permitAll()
                .pathMatchers("/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .addFilterAt(new JwtAuthenticationWebFilter(publicKey), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
