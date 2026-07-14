package com.iroit.order_service.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.iroit.order_service.models.Order;

@Component
public class OrderEventProducer {

  public static final String TOPIC = "order-events";

  @Autowired
  private KafkaTemplate<String, OrderEvent> kafkaTemplate;

  public void publish(String eventType, Order order) {
    kafkaTemplate.send(TOPIC, order.getOrderId().toString(), new OrderEvent(eventType, order));
  }

}
