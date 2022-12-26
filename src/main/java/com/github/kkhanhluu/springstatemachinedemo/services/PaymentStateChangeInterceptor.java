package com.github.kkhanhluu.springstatemachinedemo.services;

import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import com.github.kkhanhluu.springstatemachinedemo.domain.Payment;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentEvent;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentState;
import com.github.kkhanhluu.springstatemachinedemo.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
	private final PaymentRepository paymentRepository;

	@Override
	public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
			Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine,
			StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
		Optional.ofNullable(message).ifPresent(msg -> {
			Optional
					.ofNullable(Long.parseLong((String) msg.getHeaders().getOrDefault(PaymentServiceImpl.HEADER_ID, "-1L")))
					.ifPresent(paymentId -> {
						System.out.println("Interceptio was called: " + state.getId());
						Payment payment = paymentRepository.findById(paymentId).orElseThrow();
						payment.setState(state.getId());
						paymentRepository.save(payment);
					});
		});
	}

}
