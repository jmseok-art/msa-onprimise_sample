package com.example.userservice.service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.JwtDto;
import com.example.userservice.dto.LoginDto;
import com.example.userservice.dto.RefreshToken;
import com.example.userservice.dto.SignUpDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private PrivateKey key;
    
    private final long ACCESS_TOKEN_EXPIRE_TIME; 

    private final long REFRESH_TOKEN_EXPIRE_TIME; 


    public UserServiceImpl(@Value("${jwt.expire-time.access-token}") long accessTokenExpireTime,
                      @Value("${jwt.expire-time.refresh-token}") long refreshTokenExpireTime,
                      @Value("file:/app/secret/private_key_pkcs8.pem") Resource privateKey,
                      UserRepository userRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      PasswordEncoder passwordEncoder) throws IOException {
        this.userRepository = userRepository;  
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;                 
        this.ACCESS_TOKEN_EXPIRE_TIME = accessTokenExpireTime;
        this.REFRESH_TOKEN_EXPIRE_TIME = refreshTokenExpireTime;
        makeKey(new String(privateKey.getInputStream().readAllBytes()));
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
    }

    @Override
    public JwtDto login(LoginDto loginDto) {
        
        UserDto userDto = userRepository.findById(loginDto.getEmail())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));
        
        // 사용자가 입력한 비밀번호와 저장된 비밀번호 비교
        if(!passwordEncoder.matches(loginDto.getPassword(), userDto.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 인증이 완료되면 JWT 토큰 생성
        return generateToken(userDto);    
    }

    @Override
    public String logout(UserDto userDto) {

        // 저장된 리프레시 토큰 삭제
        refreshTokenRepository.deleteById(userDto.getUsername());
        return "Logout Success";
    }

    @Override
    public JwtDto refresh(String refreshToken) {
 
        // 리프레시 토큰에서 이메일 추출
        String email = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject()
                .toString();
        
        // 저장된 리프레시 토큰과 일치하는지 확인
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(email).orElseThrow(() -> new RuntimeException("리프레시 토큰이 존재하지 않습니다."));
        if(!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 일치하지 않습니다.");
        }

        // 새로운 토큰 생성
        return generateToken(
            UserDto.builder()
            .mail(savedRefreshToken.getEmail())
            .roles(savedRefreshToken.getRoles().stream()
                .map(UserDto.Role::valueOf)
                .collect(Collectors.toSet()))
            .build()
        );
    }

    @Override
    public String signUp(SignUpDto signUpDto) {

        UserDto userDto = UserDto.builder()
            .mail(signUpDto.getEmail())
            .password(passwordEncoder.encode(signUpDto.getPassword()))
            .roles(Set.of(UserDto.Role.ROLE_USER))
            .build();

        userRepository.save(userDto);
            
        return "SignUp Success";
    }

    private void makeKey(String privateKey) {

        try {

            log.info("Private Key before processing: {}", privateKey);
            privateKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            key = kf.generatePrivate(keySpecPKCS8);

            
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    private JwtDto generateToken(UserDto userDto) {

        List<String> roles = userDto.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // accessToken 생성
        String accessToken = Jwts.builder()
                .setSubject(userDto.getUsername())
                .claim("role", roles)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key)
                .compact();
        
        String refreshToken = Jwts.builder()
                .setSubject(userDto.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME)) // 7일
                .signWith(key)
                .compact();

        // RefreshToken Redis 저장
        RefreshToken refreshTokens = RefreshToken.builder()
            .email(userDto.getUsername())
            .roles(roles)
            .token(refreshToken)
            .expiredTime(REFRESH_TOKEN_EXPIRE_TIME / 1000) // 초 단위로 저장
            .build();

        refreshTokenRepository.save(refreshTokens);


        return JwtDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

}
