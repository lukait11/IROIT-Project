package com.iroit.order_service.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderTest {

  @Test
  void noArgConstructor_requiredByJpa_leavesFieldsNull() {
    Order order = new Order();

    assertThat(order.getOrderId()).isNull();
    assertThat(order.getUserId()).isNull();
    assertThat(order.getProduct()).isNull();
  }

}
