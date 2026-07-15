package com.iroit.notification_service.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.iroit.notification_service.models.Notification;
import com.iroit.notification_service.repositories.NotificationRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
class OrderEventConsumerTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private RestClient userServiceClient;

  @Mock
  private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private RestClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private RestClient.ResponseSpec responseSpec;

  @InjectMocks
  private OrderEventConsumer orderEventConsumer;

  private static final String MESSAGE = "{\"eventType\":\"ORDER_CREATED\",\"order\":{\"orderId\":42,\"userId\":1,"
      + "\"product\":\"Keyboard\",\"quantity\":2,\"orderDate\":1775260800000}}";

  private void mockRestClientChain() {
    when(userServiceClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(eq("/{id}"), any(Object[].class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void onOrderEvent_userServiceReachable_savesPersonalizedNotification() {
    mockRestClientChain();
    UserDto user = new UserDto();
    user.setUserId(1L);
    user.setFirstName("Jane");
    user.setLastName("Doe");
    when(responseSpec.body(UserDto.class)).thenReturn(user);

    orderEventConsumer.onOrderEvent(MESSAGE);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, timeout(2000)).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getOrderId()).isEqualTo(42L);
    assertThat(saved.getUserId()).isEqualTo(1L);
    assertThat(saved.getMessage()).contains("Jane Doe");
  }

  @Test
  void onOrderEvent_userServiceUnreachable_savesFallbackNotification() {
    mockRestClientChain();
    when(responseSpec.body(UserDto.class)).thenThrow(new RuntimeException("connection refused"));

    orderEventConsumer.onOrderEvent(MESSAGE);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, timeout(2000)).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getOrderId()).isEqualTo(42L);
    assertThat(saved.getMessage()).contains("user details unavailable");
  }

  @Test
  void onOrderEvent_nonCreatedEventType_isIgnored() {
    orderEventConsumer.onOrderEvent("{\"eventType\":\"ORDER_DELETED\",\"order\":{\"orderId\":42}}");

    verify(notificationRepository, never()).save(any());
  }

  @Test
  void onOrderEvent_malformedJson_isCaughtAndNotPropagated() {
    orderEventConsumer.onOrderEvent("not valid json");

    verify(notificationRepository, never()).save(any());
  }

}
