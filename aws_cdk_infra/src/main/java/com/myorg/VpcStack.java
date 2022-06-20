package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcStack extends Stack {

    private final Vpc vpc;

    public VpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        /*
        * O objeto abaixo com esses parâmetros vai criar: 2 subnets (2 pública e 2 privada), route table, fará associações
        * entre a subnets e route tables, natGateway, internetGateway, EIP.
        * Todos os recursos tem um prefixo AWS:EC2. Ex.: AWS::EC2::RouteTable, AWS::EC2::VPCGatewayAttachment
        * */
        this.vpc = Vpc.Builder.create(this, "Vpc01")
                .maxAzs(3)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
