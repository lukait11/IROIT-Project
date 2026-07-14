package com.iroit.payment_service.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.sql.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.iroit.payment_service.exceptions.InvalidRequestException;
import com.iroit.payment_service.exceptions.ResourceNotFoundException;
import com.iroit.payment_service.models.Payment;
import com.iroit.payment_service.services.PaymentService;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private PaymentService paymentService;

  @Test
  void getPayments_returnsOkWithBody() {
    when(paymentService.getPayments()).thenReturn(java.util.List.of(
        new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01"))));

    mvc.get().uri("/")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("PENDING");
  }

  @Test
  void getPayment_found_returnsOk() {
    when(paymentService.getPaymentById(1L))
        .thenReturn(new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01")));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("PENDING");
  }

  @Test
  void getPayment_notFound_returns404() {
    when(paymentService.getPaymentById(1L)).thenThrow(new ResourceNotFoundException("The payment was not found!"));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void createPayment_returnsOk() {
    Payment payment = new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01"));
    when(paymentService.createPayment(any())).thenReturn(payment);

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"orderId\":1,\"amount\":49.99,\"status\":\"PENDING\",\"paymentDate\":\"2026-01-01\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void createPayment_invalidData_returns400() {
    when(paymentService.createPayment(any())).thenThrow(new InvalidRequestException("Invalid payment data supplied!"));

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}")
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void updatePayment_returnsOk() {
    Payment payment = new Payment(1L, 49.99, "COMPLETED", Date.valueOf("2026-01-02"));
    when(paymentService.updatePayment(anyLong(), any())).thenReturn(payment);

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"orderId\":1,\"amount\":49.99,\"status\":\"COMPLETED\",\"paymentDate\":\"2026-01-02\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("COMPLETED");
  }

  @Test
  void updatePayment_notFound_returns404() {
    when(paymentService.updatePayment(anyLong(), any())).thenThrow(new ResourceNotFoundException("The payment was not found!"));

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"orderId\":1,\"amount\":49.99,\"status\":\"COMPLETED\",\"paymentDate\":\"2026-01-02\"}")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void deletePayment_returnsOk() {
    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void deletePayment_notFound_returns404() {
    org.mockito.Mockito.doThrow(new ResourceNotFoundException("The payment was not found!"))
        .when(paymentService).deletePayment(1L);

    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

}
