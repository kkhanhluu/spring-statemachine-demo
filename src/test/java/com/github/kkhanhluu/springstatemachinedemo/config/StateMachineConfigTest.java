package com.github.kkhanhluu.springstatemachinedemo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentEvent;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentState;

import reactor.core.publisher.Mono;

@SpringBootTest
public class StateMachineConfigTest {
	@Autowired
	StateMachineFactory<PaymentState, PaymentEvent> factory;

	@Test
	void testNewStateMachine() {
		StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine();
		stateMachine.startReactively().block();
		System.err.println(stateMachine.getState());
		stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE).build())).subscribe();
		System.err.println(stateMachine.getState().toString());
		stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED).build())).subscribe();
		System.err.println(stateMachine.getState().toString());
	}
}
