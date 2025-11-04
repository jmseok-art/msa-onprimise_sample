package com.example.userservice.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.LoginDto;
import com.example.userservice.dto.SignUpDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public String login(LoginDto loginDto) {
        
       UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());


        Authentication authentication =  authenticationManager.authenticate(authenticationToken);

        String jwtToken = jwtService.generateToken(authentication);

        return jwtToken;
        
    }

    public String SignUp(SignUpDto signUpDto) {

        UserDto userDto = UserDto.builder()
            .mail(signUpDto.getEmail())
            .password(passwordEncoder.encode(signUpDto.getPassword()))
            .roles(Set.of(UserDto.Role.ROLE_USER))
            .build();

        userRepository.save(userDto);
            
        return "SignUp Success";
    }
}
