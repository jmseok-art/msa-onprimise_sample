package com.example.productservice.controller;

import com.example.productservice.dto.ProductDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "상품 API", description = "상품 관련 API입니다")
public class ProductController {

    @GetMapping
    @Operation(summary = "상품 리스트 조회", description = "모든 상품의 리스트를 조회합니다. 상품번호, 상품명, 가격 정보를 포함합니다.")
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    public List<ProductDto> getProducts() {

        return List.of(
            ProductDto.builder().id("1").name("MacBook Pro").price(3500000).build(),
            ProductDto.builder().id("2").name("iPad Air").price(900000).build(),
            ProductDto.builder().id("3").name("Galaxy S24").price(1200000).build()
        );
    }

}