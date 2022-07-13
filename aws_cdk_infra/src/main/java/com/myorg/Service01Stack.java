package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
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

import java.util.HashMap;
import java.util.Map;

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
                                        .image(ContainerImage.fromRegistry("rochards/java-app-aws-projeto01:2.0.0")) // caminho da imagem
                                        // e tag do dockerhub
                                        .containerPort(8080) // porta da nossa aplicacao springboot dentro do container
                                        .logDriver(
                                                LogDriver.awsLogs( // os logs serão direcionados para o cloud watch
                                                        AwsLogDriverProps.builder()
                                                                .logGroup(
                                                                        LogGroup.Builder.create(this, "Service01LogGroup")
                                                                                .logGroupName("Service01")
                                                                                // abaixo indica que quando eu apagar a stack, os logs tbm serão
                                                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                                                .build()
                                                                )
                                                                .streamPrefix("Service01")
                                                                .build()
                                                )
                                        )
                                        .environment(retrieveAndConfigureRdsStackParameters())
                                        .build()
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
                        .maxCapacity(3)
                        .build()
        );

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50) // 50 % de utilizacao da média das instâncias nos intervalos definidos abaixo
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());
    }

    private Map<String, String> retrieveAndConfigureRdsStackParameters() {
        Map<String, String> envVariables = new HashMap<>();
        // As variáveis abaixo são padrões do spring boot
        // o nome rds-endpoint foi definido na classe RdsStack
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint")
                + ":3306/aws_projeto01?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin"); // admin tbm foi definido na classe RdsStack
        // o nome rds-password foi definido na classe RdsStack
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));

        return envVariables;
    }
}
