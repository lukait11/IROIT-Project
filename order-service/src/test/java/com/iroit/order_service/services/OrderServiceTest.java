package com.iroit.order_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroit.order_service.events.OrderEventProducer;
import com.iroit.order_service.exceptions.InvalidRequestException;
import com.iroit.order_service.exceptions.ResourceNotFoundException;
import com.iroit.order_service.models.Order;
import com.iroit.order_service.repositories.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderEventProducer orderEventProducer;

  @InjectMocks
  private OrderService orderService;

  @Test
  void getOrders_returnsWhateverTheRepositoryHas() {
    Order order = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));
    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<Order> result = orderService.getOrders();

    assertThat(result).containsExactly(order);
  }

  @Test
  void getOrderById_negativeId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> orderService.getOrderById(-1L))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void getOrderById_notFound_throwsResourceNotFoundException() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getOrderById(1L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getOrderById_found_returnsOrder() {
    Order order = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Order result = orderService.getOrderById(1L);

    assertThat(result).isEqualTo(order);
  }

  @Test
  void createOrder_nullOrder_throwsInvalidRequestException() {
    assertThatThrownBy(() -> orderService.createOrder(null))
        .isInstanceOf(InvalidRequestException.class);

    verify(orderRepository, never()).save(any());
    verify(orderEventProducer, never()).publish(any(), any());
  }

  @Test
  void createOrder_validOrder_savesAndPublishesCreatedEvent() {
    Order order = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));
    when(orderRepository.save(order)).thenReturn(order);

    Order result = orderService.createOrder(order);

    assertThat(result).isEqualTo(order);
    verify(orderEventProducer).publish("ORDER_CREATED", order);
  }

  @Test
  void updateOrder_invalidId_throwsInvalidRequestException() {
    Order order = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));

    assertThatThrownBy(() -> orderService.updateOrder(-1L, order))
        .isInstanceOf(InvalidRequestException.class);

    verify(orderRepository, never()).save(any());
    verify(orderEventProducer, never()).publish(any(), any());
  }

  @Test
  void updateOrder_nullBody_throwsInvalidRequestException() {
    assertThatThrownBy(() -> orderService.updateOrder(1L, null))
        .isInstanceOf(InvalidRequestException.class);

    verify(orderRepository, never()).save(any());
    verify(orderEventProducer, never()).publish(any(), any());
  }

  @Test
  void updateOrder_notFound_throwsResourceNotFoundException_andDoesNotPublish() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());
    Order order = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));

    assertThatThrownBy(() -> orderService.updateOrder(1L, order))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(orderRepository, never()).save(any());
    verify(orderEventProducer, never()).publish(any(), any());
  }

  @Test
  void updateOrder_existingOrder_savesTheFoundEntityAndPublishesUpdatedEvent() {
    Order existing = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Order incoming = new Order(1L, "Mouse", 5, LocalDate.of(2026, Month.FEBRUARY, 2));
    Order result = orderService.updateOrder(1L, incoming);

    ArgumentCaptor<Order> savedCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository, times(1)).save(savedCaptor.capture());

    // Regression guard: must save the entity fetched via findById (keeps the existing
    // row's ID), not the detached incoming request body.
    assertThat(savedCaptor.getValue()).isSameAs(existing);
    assertThat(result.getProduct()).isEqualTo("Mouse");
    assertThat(result.getQuantity()).isEqualTo(5);
    verify(orderEventProducer).publish("ORDER_UPDATED", existing);
  }

  @Test
  void deleteOrder_invalidId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> orderService.deleteOrder(-1L))
        .isInstanceOf(InvalidRequestException.class);

    verify(orderRepository, never()).deleteById(any());
    verify(orderEventProducer, never()).publish(any(), any());
  }

  @Test
  void deleteOrder_notFound_throwsResourceNotFoundException_andDoesNotPublish() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.deleteOrder(1L))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(orderRepository, never()).deleteById(any());
    verify(orderEventProducer, never()).publish(any(), any());
  }

  @Test
  void deleteOrder_existing_deletesItAndPublishesDeletedEvent() {
    Order existing = new Order(1L, "Keyboard", 2, LocalDate.of(2026, Month.JANUARY, 1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));

    orderService.deleteOrder(1L);

    verify(orderRepository).deleteById(1L);
    verify(orderEventProducer).publish("ORDER_DELETED", existing);
  }

}
