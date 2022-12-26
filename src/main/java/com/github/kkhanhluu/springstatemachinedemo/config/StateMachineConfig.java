package com.github.kkhanhluu.springstatemachinedemo.config;

import java.util.EnumSet;
import java.util.Random;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentEvent;
import com.github.kkhanhluu.springstatemachinedemo.domain.PaymentState;
import com.github.kkhanhluu.springstatemachinedemo.services.PaymentServiceImpl;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@EnableStateMachineFactory
@Slf4j
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

	@Override
	public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
		states.withStates().initial(PaymentState.NEW).states(EnumSet.allOf(PaymentState.class)).end(PaymentState.AUTH)
				.end(PaymentState.PRE_AUTH_ERROR).end(PaymentState.AUTH_ERROR);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
		transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
				.action(preAuthAction()).guard(paymentIdGuard())
				.and().withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH)
				.event(PaymentEvent.PRE_AUTH_APPROVED)
				.and().withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR)
				.event(PaymentEvent.PRE_AUTH_DECLINED);
	}

	@Override
	public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
		StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
			@Override
			public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
				log.info(String.format("stateChanged(from: %s, to: %s)", from, to));
			}
		};

		config.withConfiguration()
				.listener(adapter);
	}

	private Guard<PaymentState, PaymentEvent> paymentIdGuard() {
		return context -> {
			return context.getMessageHeader(PaymentServiceImpl.HEADER_ID) != null;
		};
	}

	public Action<PaymentState, PaymentEvent> preAuthAction() {
		return context -> {
			System.out.println("Preauth was called");
			if (new Random().nextInt(10) < 8) {
				System.out.println("Approved");
				context.getStateMachine().sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
						.setHeader(PaymentServiceImpl.HEADER_ID, context.getMessageHeader(PaymentServiceImpl.HEADER_ID))
						.build())).subscribe();
			} else {
				System.out.println("Declined! No credit!!!!");
				context.getStateMachine().sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
						.setHeader(PaymentServiceImpl.HEADER_ID, context.getMessageHeader(PaymentServiceImpl.HEADER_ID))
						.build())).subscribe();
			}
		};
	}
}
