package com.example.userservice.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.UserDto;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {

    private PrivateKey key;
    
    private final long EXPIRE_TIME = 86400000; // 1 day in milliseconds

    public JwtService() {
        makeKey();
    }

    private void makeKey() {
        try {
            String privateKey = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("private_key_pkcs8.pem").toURI())));
            privateKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            key = kf.generatePrivate(keySpecPKCS8);

            
        } catch (IOException | URISyntaxException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        } 
    }

    public String generateToken(Authentication authentication) {

        UserDto userDto = (UserDto)authentication.getPrincipal();

        // accessToken 생성
        String accessToken = Jwts.builder()
                .claim("email", userDto.getUsername())
                .claim("role", userDto.getAuthorities())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(key)
                .compact();


        return accessToken;
    }

}
