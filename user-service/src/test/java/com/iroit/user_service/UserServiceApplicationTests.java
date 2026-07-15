package com.iroit.user_service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

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

import com.iroit.user_service.models.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class UserServiceApplicationTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void fullUserLifecycle_createReadUpdateDeleteAllWorkAgainstARealDatabase() {
    User newUser = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));

    ResponseEntity<User> createResponse = restTemplate.postForEntity("/", newUser, User.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    User created = createResponse.getBody();
    assertThat(created).isNotNull();
    assertThat(created.getUserId()).isNotNull();
    assertThat(created.getFirstName()).isEqualTo("Ada");

    Long id = created.getUserId();

    ResponseEntity<User> getResponse = restTemplate.getForEntity("/" + id, User.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getFirstName()).isEqualTo("Ada");

    ResponseEntity<User[]> listResponse = restTemplate.getForEntity("/", User[].class);
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).hasSize(1);

    User update = new User("Augusta", "King", LocalDate.of(1815, Month.DECEMBER, 10));
    restTemplate.put("/" + id, update);

    ResponseEntity<User> afterUpdate = restTemplate.getForEntity("/" + id, User.class);
    assertThat(afterUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(afterUpdate.getBody().getFirstName()).isEqualTo("Augusta");
    assertThat(afterUpdate.getBody().getLastName()).isEqualTo("King");

    // Regression guard: updating must not create a second row.
    ResponseEntity<User[]> listAfterUpdate = restTemplate.getForEntity("/", User[].class);
    assertThat(listAfterUpdate.getBody()).hasSize(1);

    restTemplate.delete("/" + id);

    ResponseEntity<String> afterDelete = restTemplate.getForEntity("/" + id, String.class);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getUser_nonExistentId_returns404() {
    ResponseEntity<String> response = restTemplate.getForEntity("/" + Long.MAX_VALUE, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteUser_nonExistentId_returns404() {
    ResponseEntity<Void> response = restTemplate.exchange(
        "/" + Long.MAX_VALUE, org.springframework.http.HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

}
