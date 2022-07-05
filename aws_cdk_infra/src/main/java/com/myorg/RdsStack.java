package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnParameter;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.CredentialsFromUsernameOptions;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.MySqlInstanceEngineProps;
import software.amazon.awscdk.services.rds.MysqlEngineVersion;
import software.constructs.Construct;

import java.util.Collections;

public class RdsStack extends Stack {

    private final DatabaseInstance databaseInstance;

    public RdsStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, vpc, null);
    }

    public RdsStack(final Construct scope, final String id, final Vpc vpc, final StackProps props) {
        super(scope, id, props);

        this.databaseInstance = configureRdsInstance(vpc, id);
    }

    private DatabaseInstance configureRdsInstance(Vpc vpc, String id) {
        return DatabaseInstance.Builder
                .create(this, "Rds01")
                .instanceIdentifier("aws-projeto01-db")
                .engine(DatabaseInstanceEngine.mysql(
                        MySqlInstanceEngineProps.builder()
                                .version(MysqlEngineVersion.VER_5_7)
                                .build()
                ))
                .vpc(vpc)
                .credentials(createRdsUsername())
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .multiAz(false)
                .allocatedStorage(10) // GB
                .securityGroups(Collections.singletonList(configureSecurityGroup(id, vpc)))
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets())
                        .build()
                ).build();
    }

    private CfnParameter createRdsPasswordParameter() {
        return CfnParameter.Builder
                .create(this, "databasePassword")
                .type("String")
                .description("Parametro de password para o database")
                .build();
    }

    private Credentials createRdsUsername() {
        return Credentials.fromUsername("admin",
                CredentialsFromUsernameOptions.builder()
                        .password(SecretValue.plainText(createRdsPasswordParameter().getValueAsString()))
                        .build()
        );
    }

    private ISecurityGroup configureSecurityGroup(String id, Vpc vpc) {
        // Apenas instancias dentro da VPC conseguirao acessar o banco de dados na porta configurada abaixo
        ISecurityGroup iSecurityGroup = SecurityGroup
                .fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306));

        return iSecurityGroup;
    }

    public CfnOutput getRdsEndpoint() {
        return CfnOutput.Builder
                .create(this, "rds-endpoint")
                .exportName("rds-endpoint")
                .value(this.databaseInstance.getDbInstanceEndpointAddress())
                .build();
    }

    public CfnOutput getRdsPassword() {
        return CfnOutput.Builder
                .create(this, "rds-password")
                .exportName("rds-password")
                .value(createRdsPasswordParameter().getValueAsString())
                .build();
    }
}
