package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class SqsStack extends Stack {

    private final Queue productEventsQueue;

    public SqsStack(Construct scope, String id, SnsTopic productEventsTopic) {
        this(scope, id, null, productEventsTopic);
    }

    public SqsStack(Construct scope, String id, StackProps props, SnsTopic productEventsTopic) {
        super(scope, id, props);

        productEventsQueue = createProductEventsQueue();
        createSnsSubscription(productEventsTopic);
    }

    private Queue createProductEventsQueue() {
        return Queue.Builder.create(this, "ProductEvents")
                .queueName("product-events")
                .deadLetterQueue(createDeadLetterQueue())
                .build();
    }

    private DeadLetterQueue createDeadLetterQueue() {
        // mensagens que não conseguiram ser tratadas corretamente pela aplicação consumidora serão enviadas para esta fila
        return DeadLetterQueue.builder()
                .queue(
                        Queue.Builder.create(this, "ProductEventsDlq")
                                .queueName("product-events-dlq")
                                .build()
                )
                .maxReceiveCount(3) // quantos erros devem acontecer antes da mensagem ser enviada para cá
                .build();
    }

    private void createSnsSubscription(SnsTopic productEventsTopic) {
        productEventsTopic.getTopic().addSubscription(
                SqsSubscription.Builder
                        .create(productEventsQueue)
                        .build()
        );
    }

    public Queue getProductEventsQueue() {
        return productEventsQueue;
    }
}
