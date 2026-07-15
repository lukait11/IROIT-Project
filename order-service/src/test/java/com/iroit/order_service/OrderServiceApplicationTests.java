package com.iroit.order_service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import com.iroit.order_service.models.Order;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class OrderServiceApplicationTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  @ServiceConnection
  static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.1");

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void fullOrderLifecycle_createReadUpdateDeleteAllWorkAgainstARealDatabase() {
    Order newOrder = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));

    ResponseEntity<Order> createResponse = restTemplate.postForEntity("/", newOrder, Order.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Order created = createResponse.getBody();
    assertThat(created).isNotNull();
    assertThat(created.getOrderId()).isNotNull();
    assertThat(created.getProduct()).isEqualTo("Keyboard");

    Long id = created.getOrderId();

    ResponseEntity<Order> getResponse = restTemplate.getForEntity("/" + id, Order.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getProduct()).isEqualTo("Keyboard");

    ResponseEntity<Order[]> listResponse = restTemplate.getForEntity("/", Order[].class);
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).filteredOn(o -> o.getOrderId().equals(id)).hasSize(1);

    Order update = new Order(1L, "Mouse", 5, LocalDate.of(2026, Month.FEBRUARY, 2));
    restTemplate.put("/" + id, update);

    ResponseEntity<Order> afterUpdate = restTemplate.getForEntity("/" + id, Order.class);
    assertThat(afterUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(afterUpdate.getBody().getProduct()).isEqualTo("Mouse");
    assertThat(afterUpdate.getBody().getQuantity()).isEqualTo(5);

    // Regression guard: updating must not create a second row for this order.
    // (List size is checked by filtering on this test's own ID, not the whole
    // table, since other e2e tests share the same Postgres/Kafka containers.)
    ResponseEntity<Order[]> listAfterUpdate = restTemplate.getForEntity("/", Order[].class);
    assertThat(listAfterUpdate.getBody()).filteredOn(o -> o.getOrderId().equals(id)).hasSize(1);

    restTemplate.delete("/" + id);

    ResponseEntity<String> afterDelete = restTemplate.getForEntity("/" + id, String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createOrder_publishesEventToKafka() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singletonList("order-events"));

      Order newOrder = new Order(2L, "Monitor", 1, LocalDate.of(2026, Month.MARCH, 3));
      ResponseEntity<Order> createResponse = restTemplate.postForEntity("/", newOrder, Order.class);
      assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      Long id = createResponse.getBody().getOrderId();
      String expectedMarker = "\"orderId\":" + id;

      // The topic may already contain events from other e2e tests sharing this
      // broker, so scan every polled record for the one this test created
      // instead of assuming the first record received is ours.
      boolean found = false;
      long deadline = System.currentTimeMillis() + 15000;
      while (!found && System.currentTimeMillis() < deadline) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
        for (var consumerRecord : records) {
          if (consumerRecord.value().contains("ORDER_CREATED") && consumerRecord.value().contains(expectedMarker)) {
            assertThat(consumerRecord.value()).contains("Monitor");
            found = true;
            break;
          }
        }
      }
      assertThat(found).as("expected an ORDER_CREATED event for order %s on the topic", id).isTrue();
    }
  }

  @Test
  void getOrder_nonExistentId_returns404() {
    ResponseEntity<String> response = restTemplate.getForEntity("/" + Long.MAX_VALUE, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteOrder_nonExistentId_returns404() {
    ResponseEntity<Void> response = restTemplate.exchange(
        "/" + Long.MAX_VALUE, org.springframework.http.HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

}
