package com.iroit.notification_service.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationTest {

  @Test
  void noArgConstructor_requiredByJpa_leavesFieldsNull() {
    Notification notification = new Notification();

    assertThat(notification.getNotificationId()).isNull();
    assertThat(notification.getOrderId()).isNull();
    assertThat(notification.getUserId()).isNull();
    assertThat(notification.getMessage()).isNull();
    assertThat(notification.getCreatedAt()).isNull();
  }

}
