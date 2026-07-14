package com.iroit.order_service.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iroit.order_service.events.OrderEventProducer;
import com.iroit.order_service.exceptions.InvalidRequestException;
import com.iroit.order_service.exceptions.ResourceNotFoundException;
import com.iroit.order_service.models.Order;
import com.iroit.order_service.repositories.OrderRepository;

@Service
public class OrderService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderEventProducer orderEventProducer;

//#region Get methods
  public List<Order> getOrders() {
    return orderRepository.findAll();
  }

  public Order getOrderById(Long orderId) {
    if (orderId == null || orderId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    return orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("The order was not found!"));
  }
//#endregion

//#region Post methods
  public Order createOrder(Order order) {
    if (order == null)
      throw new InvalidRequestException("Invalid order data supplied!");

    Order result = orderRepository.save(order);
    orderEventProducer.publish("ORDER_CREATED", result);
    return result;
  }
//#endregion

//#region Put methods
  public Order updateOrder(Long orderId, Order order) {
    if (orderId == null || orderId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (order == null)
      throw new InvalidRequestException("Invalid order data supplied!");

    Order foundOrder = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("The order was not found!"));

    foundOrder.setUserId(order.getUserId());
    foundOrder.setProduct(order.getProduct());
    foundOrder.setQuantity(order.getQuantity());
    foundOrder.setOrderDate(order.getOrderDate());

    Order result = orderRepository.save(foundOrder);
    orderEventProducer.publish("ORDER_UPDATED", result);
    return result;
  }
//#endregion

//#region Delete methods
  public void deleteOrder(Long orderId) {
    if (orderId == null || orderId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    Order foundOrder = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("The order was not found!"));

    orderRepository.deleteById(orderId);
    orderEventProducer.publish("ORDER_DELETED", foundOrder);
  }
//#endregion

}
