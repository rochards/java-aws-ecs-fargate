package br.com.rochards.aws_projeto01.config.local;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// a classe abaixo existe para fins de testes utilizando a localstack
@Configuration
@Profile("local")
public class SqsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SqsConfig.class);

    private final AmazonSNS snsClient;
    private final Topic topicEventProduct;
    private final AmazonSQS sqsClient;

    @Autowired
    public SqsConfig(AmazonSNS snsClient, Topic topicEventProduct) {
        this.snsClient = snsClient;
        this.topicEventProduct = topicEventProduct;
        this.sqsClient = sqsClientConfig();
        createSnsSubscription(createQueue());
    }

    private AmazonSQS sqsClientConfig() {
        return AmazonSQSClient.builder()
                .withEndpointConfiguration(
                        // a regiao definida abaixo é a default para a localstack
                        // a porta 4566 é o gateway default da localstack
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private String createQueue() {
        String queueUrl = sqsClient.createQueue(new CreateQueueRequest("product-events")).getQueueUrl();
        LOG.info("SQS URL: {}", queueUrl);
        return queueUrl;
    }

    private void createSnsSubscription(String queueUrl) {
        Topics.subscribeQueue(snsClient, sqsClient, topicEventProduct.getTopicArn(), queueUrl);
    }
}
