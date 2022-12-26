package com.github.kkhanhluu.springstatemachinedemo.services;

import org.springframework.statemachine.StateMachine;

import com.github.kkhanhluu.springstatemachinedemo.domain.Payment;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentEvent;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentState;

public interface PaymentService {
	Payment createPayment(Payment payment);

	StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

	StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

	StateMachine<PaymentState, PaymentEvent> declineAuthorize(Long paymentId);
}
