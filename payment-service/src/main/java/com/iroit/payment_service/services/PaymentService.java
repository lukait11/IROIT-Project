package com.iroit.payment_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iroit.payment_service.exceptions.InvalidRequestException;
import com.iroit.payment_service.exceptions.ResourceNotFoundException;
import com.iroit.payment_service.models.Payment;
import com.iroit.payment_service.repositories.PaymentRepository;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;

  public PaymentService(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

//#region Get methods
  public List<Payment> getPayments() {
    return paymentRepository.findAll();
  }

  public Payment getPaymentById(Long paymentId) {
    if (paymentId == null || paymentId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    return paymentRepository.findById(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException("The payment was not found!"));
  }
//#endregion

//#region Post methods
  public Payment createPayment(Payment payment) {
    if (payment == null)
      throw new InvalidRequestException("Invalid payment data supplied!");

    return paymentRepository.save(payment);
  }
//#endregion

//#region Put methods
  public Payment updatePayment(Long paymentId, Payment payment) {
    if (paymentId == null || paymentId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (payment == null)
      throw new InvalidRequestException("Invalid payment data supplied!");

    Payment foundPayment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException("The payment was not found!"));

    foundPayment.setOrderId(payment.getOrderId());
    foundPayment.setAmount(payment.getAmount());
    foundPayment.setStatus(payment.getStatus());
    foundPayment.setPaymentDate(payment.getPaymentDate());

    return paymentRepository.save(foundPayment);
  }
//#endregion

//#region Delete methods
  public void deletePayment(Long paymentId) {
    if (paymentId == null || paymentId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (!paymentRepository.existsById(paymentId))
      throw new ResourceNotFoundException("The payment was not found!");

    paymentRepository.deleteById(paymentId);
  }
//#endregion

}
