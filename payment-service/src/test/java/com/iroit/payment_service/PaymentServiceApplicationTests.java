package com.iroit.payment_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
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

import com.iroit.payment_service.models.Payment;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class PaymentServiceApplicationTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  @ServiceConnection
  static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.1");

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void fullPaymentLifecycle_createReadUpdateDeleteAllWorkAgainstARealDatabase() {
    Payment newPayment = new Payment(1L, 49.99, "PENDING", LocalDate.of(2026, Month.JANUARY, 1));

    ResponseEntity<Payment> createResponse = restTemplate.postForEntity("/", newPayment, Payment.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Payment created = createResponse.getBody();
    assertThat(created).isNotNull();
    assertThat(created.getPaymentId()).isNotNull();
    assertThat(created.getStatus()).isEqualTo("PENDING");

    Long id = created.getPaymentId();

    ResponseEntity<Payment> getResponse = restTemplate.getForEntity("/" + id, Payment.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getAmount()).isEqualTo(49.99);

    // List assertions filter on this test's own ID rather than the whole
    // table, since other e2e tests share the same Postgres/Kafka containers.
    ResponseEntity<Payment[]> listResponse = restTemplate.getForEntity("/", Payment[].class);
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).filteredOn(p -> p.getPaymentId().equals(id)).hasSize(1);

    Payment update = new Payment(1L, 49.99, "COMPLETED", LocalDate.of(2026, Month.JANUARY, 2));
    restTemplate.put("/" + id, update);

    ResponseEntity<Payment> afterUpdate = restTemplate.getForEntity("/" + id, Payment.class);
    assertThat(afterUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(afterUpdate.getBody().getStatus()).isEqualTo("COMPLETED");

    // Regression guard: updating must not create a second row for this payment.
    ResponseEntity<Payment[]> listAfterUpdate = restTemplate.getForEntity("/", Payment[].class);
    assertThat(listAfterUpdate.getBody()).filteredOn(p -> p.getPaymentId().equals(id)).hasSize(1);

    restTemplate.delete("/" + id);

    ResponseEntity<String> afterDelete = restTemplate.getForEntity("/" + id, String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void orderCreatedEvent_createsAPendingPayment() throws Exception {
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

    AtomicReference<Payment> createdPayment = new AtomicReference<>();
    await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
      ResponseEntity<Payment[]> listResponse = restTemplate.getForEntity("/", Payment[].class);
      Payment match = null;
      for (Payment payment : listResponse.getBody()) {
        if (Long.valueOf(orderId).equals(payment.getOrderId())) {
          match = payment;
          break;
        }
      }
      assertThat(match).as("expected a payment to be created for order %s", orderId).isNotNull();
      createdPayment.set(match);
    });

    assertThat(createdPayment.get().getStatus()).isEqualTo("PENDING");
  }

  @Test
  void getPayment_nonExistentId_returns404() {
    ResponseEntity<String> response = restTemplate.getForEntity("/" + Long.MAX_VALUE, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deletePayment_nonExistentId_returns404() {
    ResponseEntity<Void> response = restTemplate.exchange(
        "/" + Long.MAX_VALUE, org.springframework.http.HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

}
