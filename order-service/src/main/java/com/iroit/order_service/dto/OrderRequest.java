package com.iroit.order_service.dto;

import java.time.LocalDate;

// Request body shape for create/update - kept separate from the Order entity
// so incoming JSON can't set persistence-only fields (e.g. orderId) via mass assignment.
public class OrderRequest {

  private Long userId;
  private String product;
  private Integer quantity;
  private LocalDate orderDate;

  public Long getUserId() {
    return userId;
  }
  public void setUserId(Long userId) {
    this.userId = userId;
  }
  public String getProduct() {
    return product;
  }
  public void setProduct(String product) {
    this.product = product;
  }
  public Integer getQuantity() {
    return quantity;
  }
  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
  public LocalDate getOrderDate() {
    return orderDate;
  }
  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

}
