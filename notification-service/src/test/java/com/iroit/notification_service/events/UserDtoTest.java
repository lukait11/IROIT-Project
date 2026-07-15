package com.iroit.notification_service.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserDtoTest {

  @Test
  void gettersAndSetters_roundTripTheAssignedValues() {
    UserDto user = new UserDto();

    user.setUserId(1L);
    user.setFirstName("Jane");
    user.setLastName("Doe");

    assertThat(user.getUserId()).isEqualTo(1L);
    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Doe");
  }

}
