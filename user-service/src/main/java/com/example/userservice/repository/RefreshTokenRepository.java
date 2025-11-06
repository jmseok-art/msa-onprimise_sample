package com.example.userservice.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.userservice.dto.RefreshToken;


public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String>{

    
}
