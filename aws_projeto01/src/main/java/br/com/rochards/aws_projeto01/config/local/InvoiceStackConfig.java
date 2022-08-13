package br.com.rochards.aws_projeto01.config.local;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class InvoiceStackConfig {
    private static final String BUCKET_NAME = "invoices";
    private static final String INVOICE_EVENTS = "invoice-events";
    private static final String LOCALSTACK_URL = "http://localhost:4566";

    private final AmazonS3 amazonS3;

    public InvoiceStackConfig() {
        amazonS3 = configS3Client();
        amazonS3.createBucket(BUCKET_NAME);
        AmazonSNS snsClient = configSnsClient();
        String s3InvoiceEventsTopicArn = createTopic(snsClient);
        AmazonSQS amazonSQS = configSqsClient();
        createQueue(snsClient, s3InvoiceEventsTopicArn, amazonSQS);
        configBucket(s3InvoiceEventsTopicArn);
    }

    public AmazonS3 configS3Client() {
        var awsCredentials = new BasicAWSCredentials("test", "test");
        return AmazonS3Client.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_URL, Regions.US_EAST_1.getName())
                )
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).enablePathStyleAccess()
                .build();
    }

    private AmazonSNS configSnsClient() {
        // a regiao definida abaixo é a default para a localstack
        // a porta 4566 é o gateway default da localstack
        return AmazonSNSClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_URL, Regions.US_EAST_1.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private String createTopic(AmazonSNS snsClient) {
        var createTopicRequest = new CreateTopicRequest(INVOICE_EVENTS);
        return snsClient.createTopic(createTopicRequest).getTopicArn();
    }

    private AmazonSQS configSqsClient() {
        return AmazonSQSClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_URL, Regions.US_EAST_1.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private void createQueue(AmazonSNS snsClient, String s3InvoiceEventsTopicArn, AmazonSQS sqsClient) {
        var invoiceEventsQueueUrl = sqsClient.createQueue(new CreateQueueRequest(INVOICE_EVENTS)).getQueueUrl();
        Topics.subscribeQueue(snsClient, sqsClient, s3InvoiceEventsTopicArn, invoiceEventsQueueUrl);
    }

    private void configBucket(String s3InvoiceEventsTopicArn) {
        var topicConfiguration = new TopicConfiguration();
        topicConfiguration.setTopicARN(s3InvoiceEventsTopicArn);
        topicConfiguration.addEvent(S3Event.ObjectCreatedByPut);

        amazonS3.setBucketNotificationConfiguration(
                BUCKET_NAME,
                new BucketNotificationConfiguration().addConfiguration("putObject", topicConfiguration)
        );
    }

    @Bean
    public AmazonS3 getAmazonS3() {
        return amazonS3;
    }
}
