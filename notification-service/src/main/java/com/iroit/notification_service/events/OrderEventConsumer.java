package com.iroit.notification_service.events;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroit.notification_service.models.Notification;
import com.iroit.notification_service.repositories.NotificationRepository;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

// Consumes order-service's "order-events" topic. For every new order, it
// reactively (RxJava, off the Kafka listener thread) calls user-service over
// REST to personalize the notification with the customer's name - the
// project's "reactive communication between services" channel. A failure to
// reach user-service must not lose the notification, so it falls back to a
// generic message instead of leaving the order unacknowledged.
@Component
public class OrderEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private RestClient userServiceClient;

  @KafkaListener(topics = "order-events", groupId = "${spring.application.name}")
  public void onOrderEvent(String message) {
    try {
      JsonNode root = objectMapper.readTree(message);

      if (!"ORDER_CREATED".equals(root.path("eventType").asText()))
        return;

      JsonNode order = root.path("order");
      Long orderId = order.path("orderId").asLong();
      Long userId = order.path("userId").asLong();
      String product = order.path("product").asText();

      Observable.fromCallable(() -> userServiceClient.get().uri("/{id}", userId).retrieve().body(UserDto.class))
          .subscribeOn(Schedulers.io())
          .subscribe(
              user -> saveNotification(orderId, userId,
                  "Order created for " + product + ", notifying " + user.getFirstName() + " " + user.getLastName()),
              error -> {
                logger.error("Failed to fetch user {} for order {} notification", userId, orderId, error);
                saveNotification(orderId, userId, "Order created for " + product + " (user details unavailable)");
              });
    } catch (Exception e) {
      logger.error("Failed to process order event: {}", message, e);
    }
  }

  private void saveNotification(Long orderId, Long userId, String message) {
    Notification notification = new Notification(orderId, userId, message, new Date(System.currentTimeMillis()));
    notificationRepository.save(notification);
    logger.info("Created notification for order {}: {}", orderId, message);
  }

}
