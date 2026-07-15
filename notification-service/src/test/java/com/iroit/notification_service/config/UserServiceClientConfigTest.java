package com.iroit.notification_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class UserServiceClientConfigTest {

  @Test
  void userServiceClient_buildsARestClientWithTheConfiguredBaseUrl() {
    RestClient restClient = new UserServiceClientConfig().userServiceClient("http://user-service:8080");

    assertThat(restClient).isNotNull();
  }

}
