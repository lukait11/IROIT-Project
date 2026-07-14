package com.iroit.order_service.events;

import com.iroit.order_service.models.Order;

public class OrderEvent {

  private String eventType;
  private Order order;

  public OrderEvent() {}

  public OrderEvent(String eventType, Order order) {
    this.eventType = eventType;
    this.order = order;
  }

  public String getEventType() {
    return eventType;
  }
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
  public Order getOrder() {
    return order;
  }
  public void setOrder(Order order) {
    this.order = order;
  }

}
