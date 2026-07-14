package com.iroit.notification_service.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iroit.notification_service.exceptions.InvalidRequestException;
import com.iroit.notification_service.exceptions.ResourceNotFoundException;
import com.iroit.notification_service.models.Notification;
import com.iroit.notification_service.repositories.NotificationRepository;

@Service
public class NotificationService {

  @Autowired
  private NotificationRepository notificationRepository;

//#region Get methods
  public List<Notification> getNotifications() {
    return notificationRepository.findAll();
  }

  public Notification getNotificationById(Long notificationId) {
    if (notificationId == null || notificationId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    return notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("The notification was not found!"));
  }
//#endregion

//#region Post methods
  public Notification createNotification(Notification notification) {
    if (notification == null)
      throw new InvalidRequestException("Invalid notification data supplied!");

    return notificationRepository.save(notification);
  }
//#endregion

//#region Put methods
  public Notification updateNotification(Long notificationId, Notification notification) {
    if (notificationId == null || notificationId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (notification == null)
      throw new InvalidRequestException("Invalid notification data supplied!");

    Notification foundNotification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("The notification was not found!"));

    foundNotification.setOrderId(notification.getOrderId());
    foundNotification.setUserId(notification.getUserId());
    foundNotification.setMessage(notification.getMessage());
    foundNotification.setCreatedAt(notification.getCreatedAt());

    return notificationRepository.save(foundNotification);
  }
//#endregion

//#region Delete methods
  public void deleteNotification(Long notificationId) {
    if (notificationId == null || notificationId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (!notificationRepository.existsById(notificationId))
      throw new ResourceNotFoundException("The notification was not found!");

    notificationRepository.deleteById(notificationId);
  }
//#endregion

}
