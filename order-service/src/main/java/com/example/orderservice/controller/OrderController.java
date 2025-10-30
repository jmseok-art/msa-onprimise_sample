package com.example.orderservice.controller;

import com.example.orderservice.client.ProductClient;
import com.example.common.dto.ProductDto;
import java.util.List;
import lombok.Generated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
   private final ProductClient productClient;

   @GetMapping({"/orders/products"})
   public List<ProductDto> getProductsFromProductService() {
      return this.productClient.getProducts();
   }

   @Generated
   public OrderController(ProductClient productClient) {
      this.productClient = productClient;
   }
}
