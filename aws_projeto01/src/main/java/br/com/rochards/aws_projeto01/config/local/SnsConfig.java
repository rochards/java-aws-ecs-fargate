package br.com.rochards.aws_projeto01.config.local;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// a classe abaixo existe para fins de testes utilizando a localstack
@Configuration
@Profile("local")
public class SnsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SnsConfig.class);

    private final AmazonSNS snsClient;
    private final String productTopicArn;

    public SnsConfig() {
        this.snsClient = snsClientConfig();
        this.productTopicArn = createTopic();

        LOG.info("SNS Topic ARN: {}", productTopicArn);
    }

    private AmazonSNS snsClientConfig() {
        return AmazonSNSClient.builder()
                .withEndpointConfiguration(
                        // a regiao definida abaixo é a default para a localstack
                        // a porta 4566 é o gateway default da localstack
                        new AwsClientBuilder
                                .EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private String createTopic() {
        // diferente do que vamos fazer utilizando o AWS CDK, estamos criando um tópico
        return snsClient.createTopic(new CreateTopicRequest("product-events")).getTopicArn();
    }

    @Bean
    public AmazonSNS getSnsClient() {
        return snsClient;
    }

    // caso vc esteja definindo vários tópicos, dê um nome para o seu bean
    @Bean
    public Topic snsProductEventsTopicConfig() {
        return new Topic().withTopicArn(productTopicArn);
    }
}
