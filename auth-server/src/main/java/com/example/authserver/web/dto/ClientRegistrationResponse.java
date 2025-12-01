package com.example.authserver.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRegistrationResponse {

    private String clientId;
    private String clientSecret;
    private Instant clientIdIssuedAt;
    private Instant clientSecretExpiresAt;
    private String clientName;
    private Set<String> grantTypes;
    private Set<String> redirectUris;
    private Set<String> scopes;
    private Set<String> authenticationMethods;
}
