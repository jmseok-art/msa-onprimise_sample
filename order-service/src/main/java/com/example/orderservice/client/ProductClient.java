package com.example.orderservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    @Autowired
    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getProductById(Long id) {
        // NOTE: Assumes a load-balanced RestTemplate is configured in the application context
        return restTemplate.getForObject("http://product-service/api/products/" + id, String.class);
    }
}
