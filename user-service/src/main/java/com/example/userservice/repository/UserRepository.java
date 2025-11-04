package com.example.userservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.userservice.dto.UserDto;

@Repository
public interface UserRepository extends CrudRepository<UserDto, String> {
    
}
