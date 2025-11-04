package com.example.userservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.LoginDto;
import com.example.userservice.dto.SignUpDto;
import com.example.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService jwtService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto request) {
        
        String jwt = jwtService.login(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        
        return ResponseEntity.ok().headers(headers).body("Login Success");
        
    }

    @PostMapping
    public String signUp(@RequestBody SignUpDto signUpDto) {
        return jwtService.SignUp(signUpDto);
        
    } 
    
}
