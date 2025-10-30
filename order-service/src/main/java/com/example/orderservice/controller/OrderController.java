package com.example.orderservice.controller;

import com.example.orderservice.client.ProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final ProductClient productClient;

    @Autowired
    public OrderController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping
    public ResponseEntity<?> listOrders() {
        // TODO: return list of orders (stub)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        // TODO: return order by id (stub)
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Object orderRequest) {
        // Example: call product service to validate product availability
        // productClient.getProductById(...)
        return ResponseEntity.ok().build();
    }
}
