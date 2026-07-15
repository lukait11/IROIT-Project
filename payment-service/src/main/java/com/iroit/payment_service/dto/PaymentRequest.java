package com.iroit.payment_service.dto;

import java.time.LocalDate;

// Request body shape for create/update - kept separate from the Payment entity
// so incoming JSON can't set persistence-only fields (e.g. paymentId) via mass assignment.
public class PaymentRequest {

  private Long orderId;
  private Double amount;
  private String status;
  private LocalDate paymentDate;

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
