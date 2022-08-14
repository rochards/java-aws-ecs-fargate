package com.myorg;

import software.amazon.awscdk.App;

public class AwsCdkInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        /* O deploy completo pode ser feito por exemplo:
         * $ cdk deploy --parameters Rds:databasePassword=.,0374koar872jkas Rds Vpc Cluster Sns Sqs DynamoDB Service01 Service02
         * ou
         * $ cdk deploy --all --parameters Rds:databasePassword=.,0374koar872jkas
         * */

        VpcStack vpcStack = new VpcStack(app, "Vpc");

        // depois de criado, vá ao serviço ECS (Elastic Container Service) para ver o cluster
        ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        /* passamos o valor da variável de password no momento do deploy. Ex:
         * cdk deploy --parameters Rds:databasePassword=.,0374koar872jkas
         * - Rds -> o id da stack definido abaixo
         * - databasePassword -> nome da variável definida dentro da stack
         * */
        RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        SnsStack snsStack = new SnsStack(app, "Sns");

        SqsStack sqsStack = new SqsStack(app, "Sqs", snsStack.getProductEventsTopic());
        sqsStack.addDependency(snsStack);

        InvoiceStack invoiceStack = new InvoiceStack(app, "InvoiceStack");

        // depois de criado, vá ao ECS e clique no cluster criado para ver os serviços
        Service01Stack service01Stack =
                new Service01Stack(app, "Service01", clusterStack.getCluster(), snsStack.getProductEventsTopic(),
                        invoiceStack.getInvoiceBucket(), invoiceStack.getS3InvoiceQueue());
        service01Stack.addDependency(clusterStack);
        service01Stack.addDependency(rdsStack);
        service01Stack.addDependency(snsStack);
        service01Stack.addDependency(invoiceStack);

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "DynamoDB");

        Service02Stack service02Stack = new Service02Stack(app, "Service02", clusterStack.getCluster(),
                sqsStack.getProductEventsQueue(), dynamoDBStack.getProductEventsTable());
        service02Stack.addDependency(clusterStack);
        service02Stack.addDependency(sqsStack);
        service02Stack.addDependency(dynamoDBStack);

        app.synth();
    }
}

