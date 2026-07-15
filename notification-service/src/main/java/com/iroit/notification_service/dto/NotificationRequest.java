package com.iroit.notification_service.dto;

import java.time.LocalDateTime;

// Request body shape for create/update - kept separate from the Notification
// entity so incoming JSON can't set persistence-only fields (e.g. notificationId)
// via mass assignment.
public record NotificationRequest(Long orderId, Long userId, String message, LocalDateTime createdAt) {
}
