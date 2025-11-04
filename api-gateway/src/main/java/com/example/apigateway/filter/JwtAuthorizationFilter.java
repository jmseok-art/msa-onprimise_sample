package com.example.apigateway.filter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.core.HttpHeaders;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthorizationFilter extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    private PublicKey key;

    public JwtAuthorizationFilter() {
        super(Config.class);
        makeKey();

    }

    private void makeKey() {
        try {
            String publicKey = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("public_key.pem").toURI())));
            publicKey = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            key = kf.generatePublic(keySpecX509);

            
        } catch (IOException | URISyntaxException |  NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        } 
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                return onError(exchange, "No Authorization header found");
            }
            
            String jwtHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String accessToken = (jwtHeader == null || !jwtHeader.startsWith("Bearer")) ?
                null : jwtHeader.replace("Bearer ", "");

            if(!isJwtValid(accessToken)){
                return onError(exchange, "JWT token is not valid");
            }
            
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private boolean isJwtValid(String jwt) {

        try{
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt).getBody(); 
        }catch (IllegalArgumentException e) {
            return false;
        }
        
        return true;
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }

}
