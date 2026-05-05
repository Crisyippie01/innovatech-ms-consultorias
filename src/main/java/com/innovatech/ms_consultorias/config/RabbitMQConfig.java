package com.innovatech.ms_consultorias.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.consultoria:consultoria.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.consultoria:consultoria.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey.consultoria:consultoria.routingkey}")
    private String routingKey;

    @Bean
    public Queue consultoriaQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange consultoriaExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding consultoriaBinding(Queue consultoriaQueue, TopicExchange consultoriaExchange) {
        return BindingBuilder
                .bind(consultoriaQueue)
                .to(consultoriaExchange)
                .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
