package com.iroit.notification_service.dto;

import java.time.LocalDateTime;

// Request body shape for create/update - kept separate from the Notification
// entity so incoming JSON can't set persistence-only fields (e.g. notificationId)
// via mass assignment.
public class NotificationRequest {

  private Long orderId;
  private Long userId;
  private String message;
  private LocalDateTime createdAt;

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
