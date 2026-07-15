package com.iroit.payment_service.dto;

import java.time.LocalDate;

// Request body shape for create/update - kept separate from the Payment entity
// so incoming JSON can't set persistence-only fields (e.g. paymentId) via mass assignment.
public record PaymentRequest(Long orderId, Double amount, String status, LocalDate paymentDate) {
}
