package com.amore.aketer.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String MESSAGE_QUEUE = "aketer.message.queue";
    public static final String MESSAGE_RETRY_QUEUE = "aketer.message.retry.queue";
    public static final String MESSAGE_DLQ = "aketer.message.dlq";

    public static final String MESSAGE_EXCHANGE = "aketer.message.exchange";
    public static final String DLQ_EXCHANGE = "aketer.message.dlq.exchange";

    public static final String ROUTING_KEY = "message.send";
    public static final String RETRY_ROUTING_KEY = "message.retry";
    public static final String DLQ_ROUTING_KEY = "message.failed";

    public static final long RETRY_DELAY_1 = 60_000;  // 60초
    public static final long RETRY_DELAY_2 = 120_000; // 120초
    public static final long RETRY_DELAY_3 = 240_000; // 240초

    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable(MESSAGE_QUEUE)
                .build();
    }

    @Bean
    public Queue messageRetryQueue() {
        return QueueBuilder.durable(MESSAGE_RETRY_QUEUE)
                .deadLetterExchange(MESSAGE_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue messageDlq() {
        return QueueBuilder.durable(MESSAGE_DLQ)
                .build();
    }

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(MESSAGE_EXCHANGE);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Binding bindingMessageQueue() {
        return BindingBuilder.bind(messageQueue())
                .to(messageExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding bindingRetryQueue() {
        return BindingBuilder.bind(messageRetryQueue())
                .to(messageExchange())
                .with(RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDlq() {
        return BindingBuilder.bind(messageDlq())
                .to(dlqExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
