package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class Service01Stack extends Stack {
    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        ApplicationLoadBalancedFargateService service01 =
                ApplicationLoadBalancedFargateService.Builder.create(this, "ALB01")
                        .serviceName("service-01")
                        .cluster(cluster)
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .desiredCount(1) // número de instâncias da nossa aplicação
                        .listenerPort(8080) // porta da nossa aplicacao springboot para acesso externo
                        .taskImageOptions(
                                ApplicationLoadBalancedTaskImageOptions.builder()
                                        .containerName("aws_projeto01")
                                        .image(ContainerImage.fromRegistry("rochards/java-app-aws-projeto01:1.0.0")) // caminho da imagem
                                        // e tag do dockerhub
                                        .containerPort(8080) // porta da nossa aplicacao springboot dentro do container
                                        .logDriver(LogDriver.awsLogs( // os logs serão direcionados para o cloud watch
                                                        AwsLogDriverProps.builder()
                                                                .logGroup(
                                                                        LogGroup.Builder.create(this, "Service01LogGroup")
                                                                                .logGroupName("Service01")
                                                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                                                .build()
                                                                )
                                                                .streamPrefix("Service01")
                                                                .build()
                                                )
                                        ).build()
                        )
                        .publicLoadBalancer(true)
                        .build();

        service01.getTargetGroup().configureHealthCheck(
                new HealthCheck.Builder()
                        .path("/actuator/health") // a minha aplicação SpringBoot tem que ter o actuator como dependência
                        .port("8080")
                        .healthyHttpCodes("200")
                        .build()
        );

        // config para escalar as instancias da aplicacao
        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(
                EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(4)
                        .build()
        );

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50) // 50 % de utilizacao da média das instâncias nos intervalos definidos abaixo
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());
    }
}
