package lumi.insert.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate; 
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 

@Configuration
public class RabbitMQConfig {

    // Message Queue Configuration
    @Bean 
    public Queue activityQueue() {
        return new Queue("activity-logs", false);
    }

    @Bean 
    public Queue transactionInvoiceQueue() {
        return new Queue("transaction-invoice-mail", false);
    }

    @Bean 
    public Queue uploadStorageQueue() {
        return new Queue("upload-storage", false);
    }

    @Bean 
    public Exchange exchange() {
        return new DirectExchange("main-exchange");
    }

    @Bean
    public Binding activityBinding(@Qualifier("activityQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with("activity-routing")
            .noargs();
    }

    @Bean
    public Binding transactionInvoiceBinding(@Qualifier("transactionInvoiceQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with("transaction-invoice-routing")
            .noargs();
    }

    @Bean
    public Binding uploadStorageBinding(@Qualifier("uploadStorageQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue)
            .to(exchange)
            .with("upload-storage-routing")
            .noargs();
    }

    // Object JSON Converter Configuration
    @Bean
    public MessageConverter jacksonConverter(){
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonConverter());

        return rabbitTemplate;
    }
}
