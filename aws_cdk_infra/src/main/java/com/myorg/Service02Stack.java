package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class Service02Stack extends Stack {

    private final ApplicationLoadBalancedFargateService service02;

    public Service02Stack(Construct scope, String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service02Stack(Construct scope, String id, StackProps props, Cluster cluster) {
        super(scope, id, props);

        this.service02 = configureService(cluster);
        configureHealthCheck();
        configureAutoScaling();
    }

    private ApplicationLoadBalancedFargateService configureService(Cluster cluster) {
        return ApplicationLoadBalancedFargateService.Builder.create(this, "ALB02")
                .serviceName("service-02")
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(1)
                .listenerPort(9090)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_projeto02")
                                .image(ContainerImage.fromRegistry("rochards/java-app-aws-projeto02:1.0.0"))
                                .containerPort(8080)
                                .logDriver(
                                        LogDriver.awsLogs(
                                                AwsLogDriverProps.builder()
                                                        .logGroup(
                                                                LogGroup.Builder.create(this, "Service02LogGroup")
                                                                        .logGroupName("Service02")
                                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                                        .build()
                                                        )
                                                        .streamPrefix("Service02")
                                                        .build()
                                        )
                                )
                                .build()
                )
                .publicLoadBalancer(true)
                .build();
    }

    private void configureHealthCheck() {
        service02.getTargetGroup().configureHealthCheck(
                new HealthCheck.Builder()
                        .path("/actuator/health")
                        .port("9090")
                        .healthyHttpCodes("200")
                        .build()
        );
    }

    private void configureAutoScaling() {
        ScalableTaskCount scalableTaskCount = service02.getService().autoScaleTaskCount(
                EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(3)
                        .build()
        );

        scalableTaskCount.scaleOnCpuUtilization(
                "Service02AutoScaling",
                CpuUtilizationScalingProps.builder()
                        .targetUtilizationPercent(50)
                        .scaleInCooldown(Duration.seconds(60))
                        .scaleOutCooldown(Duration.seconds(60))
                        .build()
        );
    }
}