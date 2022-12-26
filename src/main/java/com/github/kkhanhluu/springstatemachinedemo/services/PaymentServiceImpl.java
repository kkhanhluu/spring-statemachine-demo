package com.github.kkhanhluu.springstatemachinedemo.services;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.kkhanhluu.springstatemachinedemo.domain.Payment;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentEvent;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentState;
import com.github.kkhanhluu.springstatemachinedemo.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	public static final String HEADER_ID = "payment_id";
	private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;
	private final PaymentRepository paymentRepository;
	private final StateMachineFactory<PaymentState, PaymentEvent> factory;

	@Override
	public Payment createPayment(Payment payment) {
		payment.setState(PaymentState.NEW);
		return paymentRepository.save(payment);
	}

	@Override
	@Transactional
	public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
		StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
		sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);
		return stateMachine;
	}

	@Override
	@Transactional
	public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
		StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
		sendEvent(paymentId, stateMachine, PaymentEvent.AUTH_APPROVED);
		return stateMachine;
	}

	@Override
	@Transactional
	public StateMachine<PaymentState, PaymentEvent> declineAuthorize(Long paymentId) {
		StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
		sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTH_DECLINED);
		return stateMachine;
	}

	private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent event) {
		System.out.println("SEND EVENT: " + event);
		stateMachine
				.sendEvent(Mono.just(MessageBuilder.withPayload(event).setHeader(HEADER_ID, paymentId.toString()).build()))
				.subscribe();
	}

	private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId).orElseThrow();
		StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine(Long.toString(payment.getId()));
		stateMachine.stopReactively();

		stateMachine.getStateMachineAccessor().doWithAllRegions(stateMachineAccessor -> {
			stateMachineAccessor.addStateMachineInterceptor(paymentStateChangeInterceptor);
			stateMachineAccessor.resetStateMachineReactively(
					new DefaultStateMachineContext<>(payment.getState(), null, null, null, null)).block();
		});

		stateMachine.startReactively().block();
		return stateMachine;
	}

}
