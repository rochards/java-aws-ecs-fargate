package br.com.rochards.aws_projeto01.config.local;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

@EnableJms
@Configuration
@Profile("local")
public class JmsConfig {

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        var sqsConnectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClient.builder()
                        .withEndpointConfiguration(
                                // a regiao definida abaixo é a default para a localstack
                                // a porta 4566 é o gateway default da localstack
                                new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName())
                        )
                        .withCredentials(new DefaultAWSCredentialsProviderChain())
                        .build()
        );

        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(sqsConnectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("2"); // número de threads por fila
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE); // envia para fila aviso de msg tratada

        return factory;
    }
}
