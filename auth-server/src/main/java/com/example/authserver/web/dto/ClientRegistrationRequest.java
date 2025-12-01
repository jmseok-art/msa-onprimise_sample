package com.example.authserver.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRegistrationRequest {

    private String clientId;
    private String clientSecret;
    private String clientName;
    @Builder.Default
    private Set<String> grantTypes = new HashSet<>();
    @Builder.Default
    private Set<String> redirectUris = new HashSet<>();
    @Builder.Default
    private Set<String> scopes = new HashSet<>();
    @Builder.Default
    private Set<String> authenticationMethods = new HashSet<>();
}
