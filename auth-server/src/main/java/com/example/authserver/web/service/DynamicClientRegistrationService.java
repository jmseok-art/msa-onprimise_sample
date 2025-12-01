package com.example.authserver.web.service;

import com.example.authserver.web.dto.ClientRegistrationRequest;
import com.example.authserver.web.dto.ClientRegistrationResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicClientRegistrationService {

    private final RegisteredClientRepository registeredClientRepository;
    private final TokenSettings tokenSettings;
    private final PasswordEncoder passwordEncoder;

    // 요청 정보를 기반으로 신규 클라이언트를 저장하고 결과를 반환한다.
    public ClientRegistrationResponse register(ClientRegistrationRequest request) {
        String clientId = StringUtils.hasText(request.getClientId())
                ? request.getClientId()
                : UUID.randomUUID().toString();
        ensureClientIdAvailable(clientId);

        String rawSecret = StringUtils.hasText(request.getClientSecret())
                ? request.getClientSecret()
                : UUID.randomUUID().toString();

        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId)
            .clientIdIssuedAt(Instant.now())
            .clientSecret(passwordEncoder.encode(rawSecret))
            .clientName(resolveClientName(request.getClientName(), clientId))
            .tokenSettings(tokenSettings)
            .clientAuthenticationMethods(methods -> 
                applyAuthenticationMethods(RegisteredClient.withId("").clientId(""), request.getAuthenticationMethods()))
            .authorizationGrantTypes(types -> 
                applyGrantTypes(RegisteredClient.withId("").clientId(""), request.getGrantTypes()))
            .redirectUris(uris -> resolveValues(request.getRedirectUris()).forEach(uris::add))
            .scopes(scopes -> resolveValues(request.getScopes()).forEach(scopes::add))
            .build();
        registeredClientRepository.save(registeredClient);

        return ClientRegistrationResponse.builder()
            .clientId(registeredClient.getClientId())
            .clientSecret(rawSecret)
            .clientIdIssuedAt(registeredClient.getClientIdIssuedAt())
            .clientSecretExpiresAt(registeredClient.getClientSecretExpiresAt())
            .clientName(registeredClient.getClientName())
            .grantTypes(registeredClient.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.toSet()))
            .redirectUris(registeredClient.getRedirectUris())
            .scopes(registeredClient.getScopes())
            .authenticationMethods(registeredClient.getClientAuthenticationMethods().stream()
                .map(ClientAuthenticationMethod::getValue)
                .collect(Collectors.toSet()))
            .build();
    }

    // 저장 전에 동일한 client_id가 있는지 확인해 충돌을 막는다.
    private void ensureClientIdAvailable(String clientId) {
        if (registeredClientRepository.findByClientId(clientId) != null) {
            throw new IllegalArgumentException("이미 등록된 client_id 입니다: " + clientId);
        }
    }

    // 요청에 이름이 없으면 생성된 client_id를 이름으로 사용한다.
    private String resolveClientName(String requestedName, String fallback) {
        return StringUtils.hasText(requestedName) ? requestedName : fallback;
    }

    // 인증 방식이 없으면 client_secret_basic을 기본으로 설정한다.
    private void applyAuthenticationMethods(RegisteredClient.Builder builder, Set<String> methods) {
        Set<String> values = resolveValues(methods);
        if (values.isEmpty()) {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
            return;
        }
        values.stream()
                .map(this::mapAuthenticationMethod)
                .forEach(builder::clientAuthenticationMethod);
    }

    // 그랜트 타입이 없으면 authorization_code를 기본으로 설정한다.
    private void applyGrantTypes(RegisteredClient.Builder builder, Set<String> grantTypes) {
        Set<String> values = resolveValues(grantTypes);
        if (values.isEmpty()) {
            builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
            return;
        }
        values.stream()
                .map(this::mapGrantType)
                .forEach(builder::authorizationGrantType);
    }

    // null 대신 빈 Set을 반환해 이후 로직 처리를 단순화한다.
    private Set<String> resolveValues(Set<String> values) {
        return values == null ? Collections.emptySet() : values;
    }

    // 문자열 값을 인증 방식으로 변환하고 기본값은 basic으로 둔다.
    private ClientAuthenticationMethod mapAuthenticationMethod(String value) {
        if (!StringUtils.hasText(value)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        }
        return switch (value.toLowerCase()) {
            case "client_secret_post" -> ClientAuthenticationMethod.CLIENT_SECRET_POST;
            case "client_secret_jwt" -> ClientAuthenticationMethod.CLIENT_SECRET_JWT;
            case "private_key_jwt" -> ClientAuthenticationMethod.PRIVATE_KEY_JWT;
            case "none" -> ClientAuthenticationMethod.NONE;
            default -> ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        };
    }

    // 문자열 값을 허용된 그랜트 타입으로 변환하고 기본값은 auth code로 둔다.
    private AuthorizationGrantType mapGrantType(String value) {
        if (!StringUtils.hasText(value)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        }
        return switch (value.toLowerCase()) {
            case "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS;
            case "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN;
            case "password" -> new AuthorizationGrantType("password");
            case "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE;
            default -> new AuthorizationGrantType(value);
        };
    }
}
