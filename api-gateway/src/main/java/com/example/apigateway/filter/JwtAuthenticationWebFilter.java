package com.example.apigateway.filter;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import reactor.core.publisher.Mono;

// JWT 인증을 처리하는 WebFilter 입니다.
public class JwtAuthenticationWebFilter implements WebFilter{

    private PublicKey key;

    public JwtAuthenticationWebFilter(String publicKey) {
        makeKey(publicKey);

    }

    private void makeKey(String publicKey) {
        try {
            publicKey = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            key = kf.generatePublic(keySpecX509);

            
        } catch ( Exception e) {
            e.printStackTrace();
        } 
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
            return chain.filter(exchange);
        }
        
        String jwtHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String accessToken = (jwtHeader == null || !jwtHeader.startsWith("Bearer")) ?
            null : jwtHeader.replace("Bearer ", "");

        Claims claims = getClaims(accessToken);

        if(claims == null){
            return chain.filter(exchange);
        }

        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);
        
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                username,
                null,
                roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
            );
        
        request.mutate().header("X-Username", username);
        request.mutate().header("X-Roles", String.join(",", roles));

        return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private Claims getClaims(String jwt) {

        try{
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt).getBody(); 
        }catch (IllegalArgumentException e) {
            return null;
        }
    }
}
