package com.example.authserver.config;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AuthorizationServerConfig {
    
    @Value("$${PUBLIC_KEY}")
    private String publicKeyValue;
    @Value("$${PRIVATE_KEY}")
    private String privateKeyValue;
    @Value("${jwt.key.alias:auth-server}")
    private String keyAlias;
    
    
    // 1. Authorization Server 전용 SecurityFilterChain
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer.oidc(Customizer.withDefaults());

        http
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .httpBasic(HttpBasicConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .with(authorizationServerConfigurer, Customizer.withDefaults());

        return http.build();
    }

    // 1-2. 동적 클라이언트 등록 API 보호용 SecurityFilterChain
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityFilterChain clientRegistrationSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/oauth2/register/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/oauth2/register/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_DEVELOPER")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/oauth2/register/**")
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .httpBasic(HttpBasicConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .logout(LogoutConfigurer::disable);

        return http.build();
    }
    


    /**
     * 1. KeyStore에서 키 쌍을 로드하여 JWKSource (공개키 노출 및 개인키 서명 제공) 빈을 등록합니다.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = loadRsaKeyFromKeyStore();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private RSAKey loadRsaKeyFromKeyStore() {
        RSAPublicKey publicKey = parsePublicKey(publicKeyValue);
        RSAPrivateKey privateKey = parsePrivateKey(privateKeyValue);

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyAlias)
                .build();
    }

    private RSAPublicKey parsePublicKey(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("jwt.key.public 환경변수가 비어 있습니다.");
        }
        try {
            String sanitized = stripPemHeaders(value);
            byte[] decoded = Base64.getMimeDecoder().decode(sanitized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("공개키 문자열에서 RSA 공개키를 생성하지 못했습니다.", ex);
        }
    }

    private RSAPrivateKey parsePrivateKey(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("jwt.key.private 환경변수가 비어 있습니다.");
        }
        try {
            String sanitized = stripPemHeaders(value);
            byte[] decoded = Base64.getMimeDecoder().decode(sanitized);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("개인키 문자열에서 RSA 개인키를 생성하지 못했습니다.", ex);
        }
    }

    private String stripPemHeaders(String value) {
        return value
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim();
    }

    /**
     * 2. JWKSource를 사용하여 JWT 디코더를 구성합니다.
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 3. Access Token (JWT)에 커스텀 클레임(권한)을 추가합니다.
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())
            || OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())){
                Set<String> authorities = context.getPrincipal().getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                context.getClaims()
                        .claim("role", authorities)
                        .claim("username", context.getPrincipal().getName());
            }
        };
    }

    /**
     * 4. Access Token 및 Refresh Token의 유효 기간을 설정합니다.
     */
    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))  // Access Token: 30분
                .refreshTokenTimeToLive(Duration.ofDays(7))     // Refresh Token: 7일
                .reuseRefreshTokens(false)
                .build();
    }

    /**
     * 5. OAuth2 클라이언트 등록 (H2 JDBC 저장소).
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}