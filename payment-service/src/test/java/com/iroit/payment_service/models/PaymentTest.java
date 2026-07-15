package com.iroit.payment_service.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PaymentTest {

  @Test
  void noArgConstructor_requiredByJpa_leavesFieldsNull() {
    Payment payment = new Payment();

    assertThat(payment.getPaymentId()).isNull();
    assertThat(payment.getOrderId()).isNull();
    assertThat(payment.getStatus()).isNull();
  }

}
