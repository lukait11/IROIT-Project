package com.iroit.order_service.dto;

import java.time.LocalDate;

// Request body shape for create/update - kept separate from the Order entity
// so incoming JSON can't set persistence-only fields (e.g. orderId) via mass assignment.
public record OrderRequest(Long userId, String product, Integer quantity, LocalDate orderDate) {
}
