package br.com.fiap.fiapx.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "fiapx.videos";
    public static final String DLX = "fiapx.videos.dlx";
    public static final String PROCESSING_QUEUE = "video.processing";
    public static final String PROCESSING_DLQ = "video.processing.dlq";
    public static final String ROUTING_NOTIFICATION = "video.notify";

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue processingQueue() {
        return QueueBuilder.durable(PROCESSING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", PROCESSING_DLQ)
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
