package com.iroit.notification_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroit.notification_service.exceptions.InvalidRequestException;
import com.iroit.notification_service.exceptions.ResourceNotFoundException;
import com.iroit.notification_service.models.Notification;
import com.iroit.notification_service.repositories.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  void getNotifications_returnsWhateverTheRepositoryHas() {
    Notification notification = new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"));
    when(notificationRepository.findAll()).thenReturn(List.of(notification));

    List<Notification> result = notificationService.getNotifications();

    assertThat(result).containsExactly(notification);
  }

  @Test
  void getNotificationById_negativeId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> notificationService.getNotificationById(-1L))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void getNotificationById_notFound_throwsResourceNotFoundException() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.getNotificationById(1L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createNotification_nullNotification_throwsInvalidRequestException() {
    assertThatThrownBy(() -> notificationService.createNotification(null))
        .isInstanceOf(InvalidRequestException.class);

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void createNotification_validNotification_savesAndReturnsIt() {
    Notification notification = new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"));
    when(notificationRepository.save(notification)).thenReturn(notification);

    Notification result = notificationService.createNotification(notification);

    assertThat(result).isEqualTo(notification);
    verify(notificationRepository).save(notification);
  }

  @Test
  void updateNotification_notFound_throwsResourceNotFoundException() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.empty());
    Notification notification = new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"));

    assertThatThrownBy(() -> notificationService.updateNotification(1L, notification))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void updateNotification_existingNotification_savesTheFoundEntityWithNewFieldsNotTheIncomingOne() {
    Notification existing = new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"));
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Notification incoming = new Notification(1L, 1L, "Order shipped", Date.valueOf("2026-01-02"));
    Notification result = notificationService.updateNotification(1L, incoming);

    ArgumentCaptor<Notification> savedCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, times(1)).save(savedCaptor.capture());

    // Regression guard: the saved entity must be the one fetched via findById
    // (so it keeps the existing row's ID), not the detached incoming request body.
    assertThat(savedCaptor.getValue()).isSameAs(existing);
    assertThat(result.getMessage()).isEqualTo("Order shipped");
  }

  @Test
  void deleteNotification_notFound_throwsResourceNotFoundException() {
    when(notificationRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> notificationService.deleteNotification(1L))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(notificationRepository, never()).deleteById(any());
  }

  @Test
  void deleteNotification_existing_deletesIt() {
    when(notificationRepository.existsById(1L)).thenReturn(true);

    notificationService.deleteNotification(1L);

    verify(notificationRepository).deleteById(1L);
  }

}
