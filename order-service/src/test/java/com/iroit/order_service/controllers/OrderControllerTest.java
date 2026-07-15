package com.iroit.order_service.controllers;

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

import com.iroit.order_service.exceptions.InvalidRequestException;
import com.iroit.order_service.exceptions.ResourceNotFoundException;
import com.iroit.order_service.models.Order;
import com.iroit.order_service.services.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private OrderService orderService;

  @Test
  void getOrders_returnsOkWithBody() {
    when(orderService.getOrders()).thenReturn(java.util.List.of(
        new Order(1L, "Keyboard", 2, Date.valueOf("2026-01-01"))));

    mvc.get().uri("/")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Keyboard");
  }

  @Test
  void getOrders_unexpectedException_returns500() {
    when(orderService.getOrders()).thenThrow(new RuntimeException("boom"));

    mvc.get().uri("/")
        .assertThat()
        .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void getOrder_found_returnsOk() {
    when(orderService.getOrderById(1L))
        .thenReturn(new Order(1L, "Keyboard", 2, Date.valueOf("2026-01-01")));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Keyboard");
  }

  @Test
  void getOrder_notFound_returns404() {
    when(orderService.getOrderById(1L)).thenThrow(new ResourceNotFoundException("The order was not found!"));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void createOrder_returnsOk() {
    Order order = new Order(1L, "Keyboard", 2, Date.valueOf("2026-01-01"));
    when(orderService.createOrder(any())).thenReturn(order);

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":1,\"product\":\"Keyboard\",\"quantity\":2,\"orderDate\":\"2026-01-01\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void createOrder_invalidData_returns400() {
    when(orderService.createOrder(any())).thenThrow(new InvalidRequestException("Invalid order data supplied!"));

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}")
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateOrder_returnsOk() {
    Order order = new Order(1L, "Mouse", 5, Date.valueOf("2026-02-02"));
    when(orderService.updateOrder(anyLong(), any())).thenReturn(order);

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":1,\"product\":\"Mouse\",\"quantity\":5,\"orderDate\":\"2026-02-02\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Mouse");
  }

  @Test
  void updateOrder_notFound_returns404() {
    when(orderService.updateOrder(anyLong(), any())).thenThrow(new ResourceNotFoundException("The order was not found!"));

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":1,\"product\":\"Mouse\",\"quantity\":5,\"orderDate\":\"2026-02-02\"}")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteOrder_returnsOk() {
    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void deleteOrder_notFound_returns404() {
    org.mockito.Mockito.doThrow(new ResourceNotFoundException("The order was not found!"))
        .when(orderService).deleteOrder(1L);

    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

}
