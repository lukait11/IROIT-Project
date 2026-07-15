package com.iroit.notification_service.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.sql.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.iroit.notification_service.exceptions.InvalidRequestException;
import com.iroit.notification_service.exceptions.ResourceNotFoundException;
import com.iroit.notification_service.models.Notification;
import com.iroit.notification_service.services.NotificationService;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  void getNotifications_returnsOkWithBody() {
    when(notificationService.getNotifications()).thenReturn(java.util.List.of(
        new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"))));

    mvc.get().uri("/")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Order created");
  }

  @Test
  void getNotifications_unexpectedException_returns500() {
    when(notificationService.getNotifications()).thenThrow(new RuntimeException("boom"));

    mvc.get().uri("/")
        .assertThat()
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void getNotification_found_returnsOk() {
    when(notificationService.getNotificationById(1L))
        .thenReturn(new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01")));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Order created");
  }

  @Test
  void getNotification_notFound_returns404() {
    when(notificationService.getNotificationById(1L)).thenThrow(new ResourceNotFoundException("The notification was not found!"));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void createNotification_returnsOk() {
    Notification notification = new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"));
    when(notificationService.createNotification(any())).thenReturn(notification);

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"orderId\":1,\"userId\":1,\"message\":\"Order created\",\"createdAt\":\"2026-01-01\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void createNotification_invalidData_returns400() {
    when(notificationService.createNotification(any())).thenThrow(new InvalidRequestException("Invalid notification data supplied!"));

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}")
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateNotification_returnsOk() {
    Notification notification = new Notification(1L, 1L, "Order shipped", Date.valueOf("2026-01-02"));
    when(notificationService.updateNotification(anyLong(), any())).thenReturn(notification);

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"orderId\":1,\"userId\":1,\"message\":\"Order shipped\",\"createdAt\":\"2026-01-02\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Order shipped");
  }

  @Test
  void updateNotification_notFound_returns404() {
    when(notificationService.updateNotification(anyLong(), any())).thenThrow(new ResourceNotFoundException("The notification was not found!"));

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"orderId\":1,\"userId\":1,\"message\":\"Order shipped\",\"createdAt\":\"2026-01-02\"}")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteNotification_returnsOk() {
    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void deleteNotification_notFound_returns404() {
    org.mockito.Mockito.doThrow(new ResourceNotFoundException("The notification was not found!"))
        .when(notificationService).deleteNotification(1L);

    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

}
