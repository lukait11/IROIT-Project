package com.iroit.payment_service.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.iroit.payment_service.models.Payment;
import com.iroit.payment_service.services.PaymentService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class PaymentController {

  @Autowired
  private PaymentService paymentService;

  @GetMapping
  public ResponseEntity<List<Payment>> getPayments() {
    return ResponseEntity.ok(paymentService.getPayments());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
    return ResponseEntity.ok(paymentService.getPaymentById(id));
  }

  @PostMapping
  public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
    return ResponseEntity.ok(paymentService.createPayment(payment));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
    return ResponseEntity.ok(paymentService.updatePayment(id, payment));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deletePayment(@PathVariable Long id) {
    paymentService.deletePayment(id);
    return ResponseEntity.ok("Successfully deleted");
  }

}
