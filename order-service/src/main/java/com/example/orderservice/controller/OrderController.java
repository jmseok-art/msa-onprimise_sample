package com.example.orderservice.controller;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.kafka.OrderKafkaProducer;
import com.example.common.dto.ProductDto;
import com.example.common.event.OrderCreatedEvent;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

   private final OrderKafkaProducer orderKafkaProducer;
   private final ProductClient productClient;

   @PostMapping
   public String postMethodName(@RequestBody CreateOrderRequest request) {

      String orderId = UUID.randomUUID().toString();

      OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(orderId)
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .build();

      orderKafkaProducer.send(event);

      return  "✅ 주문 완료 (orderId = " + orderId + ")";
   }
   

   @GetMapping("/products")
   public List<ProductDto> getProductsFromProductService() {
      return this.productClient.getProducts();
   }

}
