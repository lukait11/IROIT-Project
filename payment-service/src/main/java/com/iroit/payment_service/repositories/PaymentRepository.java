package com.iroit.payment_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iroit.payment_service.models.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
