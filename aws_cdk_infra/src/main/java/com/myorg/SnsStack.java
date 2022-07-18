package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.sns.Topic;
import software.constructs.Construct;

public class SnsStack extends Stack {

    private final SnsTopic productEventsTopic;

    public SnsStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public SnsStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        this.productEventsTopic = createSnsTopic();
    }

    private SnsTopic createSnsTopic() {
        return SnsTopic.Builder.create(
                Topic.Builder.create(this, "ProductEventsTopic")
                        .topicName("product-events")
                        .build()
        ).build();
    }

    public SnsTopic getProductEventsTopic() {
        return productEventsTopic;
    }
}
