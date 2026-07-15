package com.iroit.notification_service.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;
  private Long orderId;
  private Long userId;
  private String message;
  private LocalDateTime createdAt;

  public Notification() {}

  public Notification(Long orderId, Long userId, String message, LocalDateTime createdAt) {
    this.orderId = orderId;
    this.userId = userId;
    this.message = message;
    this.createdAt = createdAt;
  }

  public Long getNotificationId() {
    return notificationId;
  }

  public Long getOrderId() {
    return orderId;
  }
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }
  public Long getUserId() {
    return userId;
  }
  public void setUserId(Long userId) {
    this.userId = userId;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

}
