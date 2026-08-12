package br.com.fiap.fiapx.processor.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "fiapx";
    public static final String DLX = "fiapx.dlx";
    public static final String VIDEO_PROCESSING_QUEUE = "video.processing";
    public static final String VIDEO_PROCESSING_DLQ = "video.processing.dlq";
    public static final String VIDEO_NOTIFICATION_QUEUE = "video.notification";
    public static final String VIDEO_NOTIFICATION_DLQ = "video.notification.dlq";

    @Bean
    TopicExchange fiapxExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    TopicExchange fiapxDlx() {
        return new TopicExchange(DLX, true, false);
    }

    @Bean
    Queue videoProcessingQueue() {
        return QueueBuilder.durable(VIDEO_PROCESSING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", VIDEO_PROCESSING_DLQ)
                .build();
    }

    @Bean
    Queue videoProcessingDlq() {
        return QueueBuilder.durable(VIDEO_PROCESSING_DLQ).build();
    }

    @Bean
    Queue videoNotificationQueue() {
        return QueueBuilder.durable(VIDEO_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", VIDEO_NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    Queue videoNotificationDlq() {
        return QueueBuilder.durable(VIDEO_NOTIFICATION_DLQ).build();
    }

    @Bean
    Binding videoProcessingBinding() {
        return BindingBuilder.bind(videoProcessingQueue()).to(fiapxExchange()).with(VIDEO_PROCESSING_QUEUE);
    }

    @Bean
    Binding videoProcessingDlqBinding() {
        return BindingBuilder.bind(videoProcessingDlq()).to(fiapxDlx()).with(VIDEO_PROCESSING_DLQ);
    }

    @Bean
    Binding videoNotificationBinding() {
        return BindingBuilder.bind(videoNotificationQueue()).to(fiapxExchange()).with(VIDEO_NOTIFICATION_QUEUE);
    }

    @Bean
    Binding videoNotificationDlqBinding() {
        return BindingBuilder.bind(videoNotificationDlq()).to(fiapxDlx()).with(VIDEO_NOTIFICATION_DLQ);
    }

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }
}
