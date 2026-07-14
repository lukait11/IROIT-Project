package com.iroit.notification_service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.util.Properties;

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
    Notification newNotification = new Notification(1L, 1L, "Order created", Date.valueOf("2026-01-01"));

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

    Notification update = new Notification(1L, 1L, "Order shipped", Date.valueOf("2026-01-02"));
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

    Notification createdNotification = null;
    long deadline = System.currentTimeMillis() + 20000;
    while (createdNotification == null && System.currentTimeMillis() < deadline) {
      ResponseEntity<Notification[]> listResponse = restTemplate.getForEntity("/", Notification[].class);
      for (Notification notification : listResponse.getBody()) {
        if (Long.valueOf(orderId).equals(notification.getOrderId())) {
          createdNotification = notification;
          break;
        }
      }
      if (createdNotification == null)
        Thread.sleep(500);
    }

    assertThat(createdNotification).as("expected a notification to be created for order %s", orderId).isNotNull();
    assertThat(createdNotification.getMessage()).contains("user details unavailable");
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
