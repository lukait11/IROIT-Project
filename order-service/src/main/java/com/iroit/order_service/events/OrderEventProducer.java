package com.iroit.order_service.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.iroit.order_service.models.Order;

@Component
public class OrderEventProducer {

  public static final String TOPIC = "order-events";

  private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);

  private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

  public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  // The order is already durably saved by the time this runs, so a broker
  // outage must not fail the request that triggered it - log and move on.
  public void publish(String eventType, Order order) {
    try {
      kafkaTemplate.send(TOPIC, order.getOrderId().toString(), new OrderEvent(eventType, order));
    } catch (Exception e) {
      logger.error("Failed to publish {} event for order {}", eventType, order.getOrderId(), e);
    }
  }

}
