package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.UtilizationScalingProps;
import software.constructs.Construct;

public class DynamoDBStack extends Stack {

    private final Table productEventsTable;

    public DynamoDBStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public DynamoDBStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        this.productEventsTable = createTable();
        configTableAutoScaling();
    }

    private Table createTable() {
        return Table.Builder.create(this, "ProductEventsTable")
                .tableName("product-events")
                .billingMode(BillingMode.PROVISIONED)
                .readCapacity(1) // read capacity e write capacity estão configurados pq o billing é PROVISIONED
                .writeCapacity(1)
                .partitionKey(
                        Attribute.builder()
                                .name("pk")
                                .type(AttributeType.STRING)
                                .build()
                )
                .sortKey(
                        Attribute.builder()
                                .name("sk")
                                .type(AttributeType.STRING)
                                .build()
                )
                .timeToLiveAttribute("ttl")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private void configTableAutoScaling() {
        productEventsTable.autoScaleReadCapacity(
                EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(4)
                        .build()
        ).scaleOnUtilization(
                UtilizationScalingProps.builder()
                        .targetUtilizationPercent(50)
                        .scaleInCooldown(Duration.seconds(30))
                        .scaleOutCooldown(Duration.seconds(30))
                        .build()
        );

        productEventsTable.autoScaleWriteCapacity(
                EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(4)
                        .build()
        ).scaleOnUtilization(
                UtilizationScalingProps.builder()
                        .targetUtilizationPercent(50)
                        .scaleInCooldown(Duration.seconds(30))
                        .scaleOutCooldown(Duration.seconds(30))
                        .build()
        );
    }

    public Table getProductEventsTable() {
        return productEventsTable;
    }
}
