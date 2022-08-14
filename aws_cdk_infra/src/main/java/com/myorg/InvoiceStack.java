package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.EventType;
import software.amazon.awscdk.services.s3.notifications.SnsDestination;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.UUID;

public class InvoiceStack extends Stack {

    private final Bucket invoiceBucket;
    private final SnsTopic s3InvoiceTopic;
    private final Queue s3InvoiceQueue;

    public InvoiceStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public InvoiceStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        invoiceBucket = createInvoiceBucket();
        s3InvoiceTopic = createSnsInvoiceTopic();
        s3InvoiceQueue = createInvoiceQueue();
        createEventNotificationBucketToSns();
        createSqsSubscriptionToSns();
    }

    private Bucket createInvoiceBucket() {
        return Bucket.Builder.create(this, "S3Invoice")
                .bucketName("invoices-" + UUID.randomUUID()) // nome do bucket precisa ser unico na Region
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private SnsTopic createSnsInvoiceTopic() {
        return SnsTopic.Builder.create(
                        Topic.Builder.create(this, "S3InvoiceTopic")
                                .topicName("s3-invoice-events")
                                .build())
                .build();
    }

    private Queue createInvoiceQueue() {
        DeadLetterQueue s3InvoiceDlq = DeadLetterQueue.builder()
                .queue(
                        Queue.Builder.create(this, "S3InvoiceDlq")
                                .queueName("s3-invoice-events-dlq")
                                .build()
                )
                .maxReceiveCount(3)
                .build();

        return Queue.Builder.create(this, "S3InvoiceQueue")
                .queueName("s3-invoice-events")
                .deadLetterQueue(s3InvoiceDlq)
                .build();
    }

    private void createEventNotificationBucketToSns() {
        invoiceBucket.addEventNotification(EventType.OBJECT_CREATED_PUT, new SnsDestination(s3InvoiceTopic.getTopic()));
    }

    private void createSqsSubscriptionToSns() {
        s3InvoiceTopic.getTopic().addSubscription(
                SqsSubscription.Builder.create(s3InvoiceQueue)
                        .build()
        );
    }

    public Bucket getInvoiceBucket() {
        return invoiceBucket;
    }

    public Queue getS3InvoiceQueue() {
        return s3InvoiceQueue;
    }
}
