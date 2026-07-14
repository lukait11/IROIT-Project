package com.iroit.order_service.models;

import java.sql.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderId;
  private Long userId;
  private String product;
  private Integer quantity;
  private Date orderDate;

  public Order() {}

  public Order(Long userId, String product, Integer quantity, Date orderDate) {
    this.userId = userId;
    this.product = product;
    this.quantity = quantity;
    this.orderDate = orderDate;
  }

  public Long getOrderId() {
    return orderId;
  }

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
  public Date getOrderDate() {
    return orderDate;
  }
  public void setOrderDate(Date orderDate) {
    this.orderDate = orderDate;
  }

}
