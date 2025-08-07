package com.company.codereview.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {

    // 通知相关队列和交换机
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";

    // 评审分配相关队列和交换机
    public static final String ASSIGNMENT_EXCHANGE = "assignment.exchange";
    public static final String ASSIGNMENT_QUEUE = "assignment.queue";
    public static final String ASSIGNMENT_ROUTING_KEY = "assignment.create";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public DirectExchange assignmentExchange() {
        return new DirectExchange(ASSIGNMENT_EXCHANGE);
    }

    @Bean
    public Queue assignmentQueue() {
        return QueueBuilder.durable(ASSIGNMENT_QUEUE).build();
    }

    @Bean
    public Binding assignmentBinding() {
        return BindingBuilder.bind(assignmentQueue())
                .to(assignmentExchange())
                .with(ASSIGNMENT_ROUTING_KEY);
    }
}