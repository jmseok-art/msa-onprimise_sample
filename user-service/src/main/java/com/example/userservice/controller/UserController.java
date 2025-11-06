package com.example.userservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.JwtDto;
import com.example.userservice.dto.LoginDto;
import com.example.userservice.dto.SignUpDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    @Value("${jwt.expire-time.refresh-token}")
    private long refreshTokenExpireTime;

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto request) {
        
        JwtDto jwtDto = userService.login(request);
        HttpHeaders headers = makeJwtHeader(jwtDto);
        
        return ResponseEntity.ok().headers(headers).body("Login Success");
        
    }
    
    @PostMapping("/users")
    public String signUp(@RequestBody SignUpDto signUpDto) {
        return userService.SignUp(signUpDto);
        
    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@CookieValue(value = "RefreshToken", defaultValue = "") String refreshToken) {
        JwtDto jwtDto = userService.refresh(refreshToken);
        
        HttpHeaders headers = makeJwtHeader(jwtDto);
        return ResponseEntity.ok().headers(headers).body("refresh Success");
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public String logout(@AuthenticationPrincipal UserDto userDto) {
        
        return userService.logout(userDto);
    }
      
    private HttpHeaders makeJwtHeader(JwtDto jwtDto) {
        HttpHeaders headers = new HttpHeaders();

        ResponseCookie responseCookie = ResponseCookie.from("RefreshToken", jwtDto.getRefreshToken())
                .path("/")
                .maxAge(refreshTokenExpireTime)
                .sameSite("Lax")
                .secure(false)
                .httpOnly(true)
                .build();

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtDto.getAccessToken());
        headers.add(HttpHeaders.SET_COOKIE, responseCookie.toString());
        return headers;
    }
    
}
