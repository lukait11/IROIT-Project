package com.iroit.order_service.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.iroit.order_service.dto.OrderRequest;
import com.iroit.order_service.models.Order;
import com.iroit.order_service.services.OrderService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public ResponseEntity<List<Order>> getOrders() {
    return ResponseEntity.ok(orderService.getOrders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrder(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getOrderById(id));
  }

  @PostMapping
  public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
    Order order = new Order(request.getUserId(), request.getProduct(), request.getQuantity(), request.getOrderDate());
    return ResponseEntity.ok(orderService.createOrder(order));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody OrderRequest request) {
    Order order = new Order(request.getUserId(), request.getProduct(), request.getQuantity(), request.getOrderDate());
    return ResponseEntity.ok(orderService.updateOrder(id, order));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return ResponseEntity.ok("Successfully deleted");
  }

}
