package com.iroit.notification_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import com.iroit.notification_service.models.Notification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class NotificationServiceApplicationTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  @ServiceConnection
  static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.1");

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void fullNotificationLifecycle_createReadUpdateDeleteAllWorkAgainstARealDatabase() {
    Notification newNotification = new Notification(1L, 1L, "Order created", LocalDateTime.of(2026, 1, 1, 0, 0));

    ResponseEntity<Notification> createResponse = restTemplate.postForEntity("/", newNotification, Notification.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Notification created = createResponse.getBody();
    assertThat(created).isNotNull();
    assertThat(created.getNotificationId()).isNotNull();
    assertThat(created.getMessage()).isEqualTo("Order created");

    Long id = created.getNotificationId();

    ResponseEntity<Notification> getResponse = restTemplate.getForEntity("/" + id, Notification.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getMessage()).isEqualTo("Order created");

    // List assertions filter on this test's own ID rather than the whole
    // table, since other e2e tests share the same Postgres/Kafka containers.
    ResponseEntity<Notification[]> listResponse = restTemplate.getForEntity("/", Notification[].class);
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).filteredOn(n -> n.getNotificationId().equals(id)).hasSize(1);

    Notification update = new Notification(1L, 1L, "Order shipped", LocalDateTime.of(2026, 1, 2, 0, 0));
    restTemplate.put("/" + id, update);

    ResponseEntity<Notification> afterUpdate = restTemplate.getForEntity("/" + id, Notification.class);
    assertThat(afterUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(afterUpdate.getBody().getMessage()).isEqualTo("Order shipped");

    // Regression guard: updating must not create a second row for this notification.
    ResponseEntity<Notification[]> listAfterUpdate = restTemplate.getForEntity("/", Notification[].class);
    assertThat(listAfterUpdate.getBody()).filteredOn(n -> n.getNotificationId().equals(id)).hasSize(1);

    restTemplate.delete("/" + id);

    ResponseEntity<String> afterDelete = restTemplate.getForEntity("/" + id, String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void orderCreatedEvent_withUnreachableUserService_stillCreatesANotificationWithFallbackMessage() throws Exception {
    // No user-service is running in this test environment, so the reactive
    // lookup triggered by the consumer is expected to fail - the consumer
    // must fall back to a generic message rather than losing the notification.
    long orderId = 4242L;
    String eventJson = "{\"eventType\":\"ORDER_CREATED\",\"order\":{\"orderId\":" + orderId
        + ",\"userId\":1,\"product\":\"Keyboard\",\"quantity\":2,\"orderDate\":1775260800000}}";

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
      producer.send(new ProducerRecord<>("order-events", String.valueOf(orderId), eventJson)).get();
    }

    AtomicReference<Notification> createdNotification = new AtomicReference<>();
    await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
      ResponseEntity<Notification[]> listResponse = restTemplate.getForEntity("/", Notification[].class);
      Notification match = null;
      for (Notification notification : listResponse.getBody()) {
        if (Long.valueOf(orderId).equals(notification.getOrderId())) {
          match = notification;
          break;
        }
      }
      assertThat(match).as("expected a notification to be created for order %s", orderId).isNotNull();
      createdNotification.set(match);
    });

    assertThat(createdNotification.get().getMessage()).contains("user details unavailable");
  }

  @Test
  void getNotification_nonExistentId_returns404() {
    ResponseEntity<String> response = restTemplate.getForEntity("/" + Long.MAX_VALUE, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteNotification_nonExistentId_returns404() {
    ResponseEntity<Void> response = restTemplate.exchange(
        "/" + Long.MAX_VALUE, org.springframework.http.HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

}
