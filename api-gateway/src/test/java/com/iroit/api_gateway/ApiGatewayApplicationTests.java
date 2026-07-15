package com.iroit.api_gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.sun.net.httpserver.HttpServer;

// There's no business logic here to unit test - this service is pure
// declarative routing config. So the meaningful test is proving the routes
// actually proxy correctly: a fake backend (plain JDK HttpServer, standing in
// for all four downstream services) echoes back the path it received, which
// lets each test assert its route's Path predicate + StripPrefix filter
// produced the right forwarded request.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ApiGatewayApplicationTests {

  private static HttpServer fakeBackend;

  @DynamicPropertySource
  static void registerFakeBackend(DynamicPropertyRegistry registry) throws IOException {
    fakeBackend = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    fakeBackend.createContext("/", exchange -> {
      String responseBody = "{\"path\":\"" + exchange.getRequestURI().getPath() + "\"}";
      byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
      exchange.close();
    });
    fakeBackend.start();

    String backendUrl = "http://localhost:" + fakeBackend.getAddress().getPort();
    registry.add("USER_SERVICE_URL", () -> backendUrl);
    registry.add("ORDER_SERVICE_URL", () -> backendUrl);
    registry.add("PAYMENT_SERVICE_URL", () -> backendUrl);
    registry.add("NOTIFICATION_SERVICE_URL", () -> backendUrl);
  }

  @AfterAll
  static void stopFakeBackend() {
    if (fakeBackend != null)
      fakeBackend.stop(0);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @ParameterizedTest
  @CsvSource({
      "/api/users/42, /42",
      "/api/orders/7, /7",
      "/api/payments/3, /3",
      "/api/notifications/1, /1"
  })
  void routesPath_stripsApiPrefix(String requestPath, String expectedForwardedPath) {
    ResponseEntity<String> response = restTemplate.getForEntity(requestPath, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"path\":\"" + expectedForwardedPath + "\"");
  }

  @Test
  void unmatchedPath_returns404() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/unknown", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

}
