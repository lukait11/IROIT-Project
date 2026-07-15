package com.iroit.payment_service.models;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long paymentId;
  private Long orderId;
  private Double amount;
  private String status;
  private LocalDate paymentDate;

  public Payment() {}

  public Payment(Long orderId, Double amount, String status, LocalDate paymentDate) {
    this.orderId = orderId;
    this.amount = amount;
    this.status = status;
    this.paymentDate = paymentDate;
  }

  public Long getPaymentId() {
    return paymentId;
  }

  public Long getOrderId() {
    return orderId;
  }
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }
  public Double getAmount() {
    return amount;
  }
  public void setAmount(Double amount) {
    this.amount = amount;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public LocalDate getPaymentDate() {
    return paymentDate;
  }
  public void setPaymentDate(LocalDate paymentDate) {
    this.paymentDate = paymentDate;
  }

}
