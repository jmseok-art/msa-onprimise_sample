package com.example.authserver.web.controller;

import com.example.authserver.web.dto.ClientRegistrationRequest;
import com.example.authserver.web.dto.ClientRegistrationResponse;
import com.example.authserver.web.service.DynamicClientRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/register")
@RequiredArgsConstructor
public class ClientRegistrationController {

    private final DynamicClientRegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientRegistrationResponse register(@RequestBody ClientRegistrationRequest request) {
        return registrationService.register(request);
    }
}
