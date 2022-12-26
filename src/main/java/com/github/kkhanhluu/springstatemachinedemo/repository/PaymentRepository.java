package com.github.kkhanhluu.springstatemachinedemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.kkhanhluu.springstatemachinedemo.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
