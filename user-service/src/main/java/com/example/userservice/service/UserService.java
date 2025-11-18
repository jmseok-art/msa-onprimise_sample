package com.example.userservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.userservice.dto.JwtDto;
import com.example.userservice.dto.LoginDto;
import com.example.userservice.dto.SignUpDto;
import com.example.userservice.dto.UserDto;


public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    // 로그인 처리 및 토큰 발급 ㅇ
    JwtDto login(LoginDto loginDto);

    // 로그아웃 처리 (리프레시 토큰 제거 등)
    String logout(UserDto userDto);

    // 리프레시 토큰으로 액세스 토큰 재발급
    JwtDto refresh(String refreshToken);

    // 회원가입 처리
    String signUp(SignUpDto signUpDto);
}
