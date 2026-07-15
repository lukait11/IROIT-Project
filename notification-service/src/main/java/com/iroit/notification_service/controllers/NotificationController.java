package com.iroit.notification_service.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.iroit.notification_service.dto.NotificationRequest;
import com.iroit.notification_service.models.Notification;
import com.iroit.notification_service.services.NotificationService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public ResponseEntity<List<Notification>> getNotifications() {
    return ResponseEntity.ok(notificationService.getNotifications());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
    return ResponseEntity.ok(notificationService.getNotificationById(id));
  }

  @PostMapping
  public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest request) {
    Notification notification = new Notification(
        request.getOrderId(), request.getUserId(), request.getMessage(), request.getCreatedAt());
    return ResponseEntity.ok(notificationService.createNotification(notification));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Notification> updateNotification(@PathVariable Long id, @RequestBody NotificationRequest request) {
    Notification notification = new Notification(
        request.getOrderId(), request.getUserId(), request.getMessage(), request.getCreatedAt());
    return ResponseEntity.ok(notificationService.updateNotification(id, notification));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
    notificationService.deleteNotification(id);
    return ResponseEntity.ok("Successfully deleted");
  }

}
