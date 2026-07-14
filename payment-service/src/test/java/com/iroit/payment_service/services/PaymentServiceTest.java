package com.iroit.payment_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroit.payment_service.exceptions.InvalidRequestException;
import com.iroit.payment_service.exceptions.ResourceNotFoundException;
import com.iroit.payment_service.models.Payment;
import com.iroit.payment_service.repositories.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private PaymentService paymentService;

  @Test
  void getPayments_returnsWhateverTheRepositoryHas() {
    Payment payment = new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01"));
    when(paymentRepository.findAll()).thenReturn(List.of(payment));

    List<Payment> result = paymentService.getPayments();

    assertThat(result).containsExactly(payment);
  }

  @Test
  void getPaymentById_negativeId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> paymentService.getPaymentById(-1L))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void getPaymentById_notFound_throwsResourceNotFoundException() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.getPaymentById(1L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createPayment_nullPayment_throwsInvalidRequestException() {
    assertThatThrownBy(() -> paymentService.createPayment(null))
        .isInstanceOf(InvalidRequestException.class);

    verify(paymentRepository, never()).save(any());
  }

  @Test
  void createPayment_validPayment_savesAndReturnsIt() {
    Payment payment = new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01"));
    when(paymentRepository.save(payment)).thenReturn(payment);

    Payment result = paymentService.createPayment(payment);

    assertThat(result).isEqualTo(payment);
    verify(paymentRepository).save(payment);
  }

  @Test
  void updatePayment_notFound_throwsResourceNotFoundException() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.empty());
    Payment payment = new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01"));

    assertThatThrownBy(() -> paymentService.updatePayment(1L, payment))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(paymentRepository, never()).save(any());
  }

  @Test
  void updatePayment_existingPayment_savesTheFoundEntityWithNewFieldsNotTheIncomingOne() {
    Payment existing = new Payment(1L, 49.99, "PENDING", Date.valueOf("2026-01-01"));
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Payment incoming = new Payment(1L, 49.99, "COMPLETED", Date.valueOf("2026-01-02"));
    Payment result = paymentService.updatePayment(1L, incoming);

    ArgumentCaptor<Payment> savedCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(savedCaptor.capture());

    // Regression guard: the saved entity must be the one fetched via findById
    // (so it keeps the existing row's ID), not the detached incoming request body.
    assertThat(savedCaptor.getValue()).isSameAs(existing);
    assertThat(result.getStatus()).isEqualTo("COMPLETED");
  }

  @Test
  void deletePayment_notFound_throwsResourceNotFoundException() {
    when(paymentRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> paymentService.deletePayment(1L))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(paymentRepository, never()).deleteById(any());
  }

  @Test
  void deletePayment_existing_deletesIt() {
    when(paymentRepository.existsById(1L)).thenReturn(true);

    paymentService.deletePayment(1L);

    verify(paymentRepository).deleteById(1L);
  }

}
