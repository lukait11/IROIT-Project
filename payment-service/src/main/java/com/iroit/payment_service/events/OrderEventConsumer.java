package com.iroit.payment_service.events;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroit.payment_service.models.Payment;
import com.iroit.payment_service.repositories.PaymentRepository;

// Consumes order-service's "order-events" topic and opens a pending payment
// for every new order. Read as raw String and parsed manually with a plain
// ObjectMapper rather than a typed JsonDeserializer, so this service stays
// decoupled from order-service's event class (there's no shared module).
@Component
public class OrderEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private PaymentRepository paymentRepository;

  @KafkaListener(topics = "order-events", groupId = "${spring.application.name}")
  public void onOrderEvent(String message) {
    try {
      JsonNode root = objectMapper.readTree(message);

      if (!"ORDER_CREATED".equals(root.path("eventType").asText()))
        return;

      Long orderId = root.path("order").path("orderId").asLong();

      Payment payment = new Payment(orderId, 0.0, "PENDING", new Date(System.currentTimeMillis()));
      paymentRepository.save(payment);
      logger.info("Created pending payment for order {}", orderId);
    } catch (Exception e) {
      logger.error("Failed to process order event: {}", message, e);
    }
  }

}
