package com.iroit.order_service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;

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

import com.iroit.order_service.models.Order;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class OrderServiceApplicationTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void fullOrderLifecycle_createReadUpdateDeleteAllWorkAgainstARealDatabase() {
    Order newOrder = new Order(1L, "Keyboard", 2, Date.valueOf("2026-01-01"));

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
    assertThat(listResponse.getBody()).hasSize(1);

    Order update = new Order(1L, "Mouse", 5, Date.valueOf("2026-02-02"));
    restTemplate.put("/" + id, update);

    ResponseEntity<Order> afterUpdate = restTemplate.getForEntity("/" + id, Order.class);
    assertThat(afterUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(afterUpdate.getBody().getProduct()).isEqualTo("Mouse");
    assertThat(afterUpdate.getBody().getQuantity()).isEqualTo(5);

    // Regression guard: updating must not create a second row.
    ResponseEntity<Order[]> listAfterUpdate = restTemplate.getForEntity("/", Order[].class);
    assertThat(listAfterUpdate.getBody()).hasSize(1);

    restTemplate.delete("/" + id);

    ResponseEntity<String> afterDelete = restTemplate.getForEntity("/" + id, String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
