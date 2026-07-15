package com.iroit.payment_service.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroit.payment_service.models.Payment;
import com.iroit.payment_service.repositories.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private OrderEventConsumer orderEventConsumer;

  @Test
  void onOrderEvent_orderCreated_opensAPendingPaymentForThatOrder() {
    String message = "{\"eventType\":\"ORDER_CREATED\",\"order\":{\"orderId\":42,\"userId\":1,"
        + "\"product\":\"Keyboard\",\"quantity\":2,\"orderDate\":1775260800000}}";

    orderEventConsumer.onOrderEvent(message);

    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(paymentCaptor.capture());

    Payment saved = paymentCaptor.getValue();
    assertThat(saved.getOrderId()).isEqualTo(42L);
    assertThat(saved.getStatus()).isEqualTo("PENDING");
  }

  @Test
  void onOrderEvent_nonCreatedEventType_isIgnored() {
    String message = "{\"eventType\":\"ORDER_DELETED\",\"order\":{\"orderId\":42}}";

    orderEventConsumer.onOrderEvent(message);

    verify(paymentRepository, never()).save(any());
  }

  @Test
  void onOrderEvent_malformedJson_isCaughtAndNotPropagated() {
    // Must not throw - a bad message must not crash the listener container.
    orderEventConsumer.onOrderEvent("not valid json");

    verify(paymentRepository, never()).save(any());
  }

}
