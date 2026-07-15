package com.iroit.order_service.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.iroit.order_service.models.Order;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

  @Mock
  private KafkaTemplate<String, OrderEvent> kafkaTemplate;

  @InjectMocks
  private OrderEventProducer orderEventProducer;

  @Test
  void publish_sendsOrderEventToTheOrderEventsTopic() {
    Order order = spy(new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1)));
    when(order.getOrderId()).thenReturn(42L);

    orderEventProducer.publish("ORDER_CREATED", order);

    ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
    verify(kafkaTemplate).send(eq(OrderEventProducer.TOPIC), eq("42"), eventCaptor.capture());

    OrderEvent event = eventCaptor.getValue();
    assertThat(event.getEventType()).isEqualTo("ORDER_CREATED");
    assertThat(event.getOrder()).isSameAs(order);
    assertThat(event.getOrder().getProduct()).isEqualTo("Keyboard");
  }

  @Test
  void publish_kafkaSendThrows_isCaughtAndNotPropagated() {
    Order order = spy(new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1)));
    when(order.getOrderId()).thenReturn(42L);
    when(kafkaTemplate.send(any(String.class), any(String.class), any(OrderEvent.class)))
        .thenThrow(new RuntimeException("broker unreachable"));

    // A broker outage must not fail the request that triggered the publish.
    assertThatCode(() -> orderEventProducer.publish("ORDER_CREATED", order)).doesNotThrowAnyException();

    verify(kafkaTemplate).send(eq(OrderEventProducer.TOPIC), eq("42"), any(OrderEvent.class));
  }

}
