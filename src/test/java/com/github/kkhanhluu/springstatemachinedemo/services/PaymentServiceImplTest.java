package com.github.kkhanhluu.springstatemachinedemo.services;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.config.StateMachineFactory;

import com.github.kkhanhluu.springstatemachinedemo.domain.Payment;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentEvent;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentState;
import com.github.kkhanhluu.springstatemachinedemo.repository.PaymentRepository;

@SpringBootTest
public class PaymentServiceImplTest {
	@Autowired
	StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

	@Autowired
	PaymentService paymentService;

	@Autowired
	PaymentRepository paymentRepository;

	Payment payment;

	@BeforeEach
	void setup() {
		payment = Payment.builder().amount(new BigDecimal("12.99")).build();
	}

	@Test
	void preAuth() {
		Payment newPayment = paymentService.createPayment(payment);
		paymentService.preAuth(newPayment.getId());
		Payment preAuthedPayment = paymentRepository.findById(newPayment.getId()).orElseThrow();
		System.out.println(preAuthedPayment);
	}
}
