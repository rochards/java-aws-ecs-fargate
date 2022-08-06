package br.com.rochards.config.local;

import br.com.rochards.repository.EventProductLogRepository;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("local")
@EnableDynamoDBRepositories(basePackageClasses = EventProductLogRepository.class)
public class DynamoDBConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoDBConfig.class);

    private final AmazonDynamoDB amazonDynamoDB;

    public DynamoDBConfig() {
        this.amazonDynamoDB = configClient();
        createTable();
    }

    public AmazonDynamoDB configClient() {
        return AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder
                                .EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    public void createTable() {
        var createTableRequest = new CreateTableRequest()
                .withTableName("product-events")
                .withAttributeDefinitions(List.of(
                        new AttributeDefinition().withAttributeName("pk").withAttributeType(ScalarAttributeType.S),
                        new AttributeDefinition().withAttributeName("sk").withAttributeType(ScalarAttributeType.S)
                ))
                .withKeySchema(List.of(
                        new KeySchemaElement().withAttributeName("pk").withKeyType(KeyType.HASH),
                        new KeySchemaElement().withAttributeName("sk").withKeyType(KeyType.RANGE)
                ))
                .withBillingMode(BillingMode.PAY_PER_REQUEST); // para nao precisar configurar o provisioned throughput

        var dynamoDB = new DynamoDB(amazonDynamoDB);
        var table = dynamoDB.createTable(createTableRequest);

        try {
            table.waitForActive();
        } catch (InterruptedException e) {
            LOG.error("Erro ao criar tabela de produtos", e);
        }
    }

    @Bean
    @Primary
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.DEFAULT;
    }


    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig config) {
        return new DynamoDBMapper(amazonDynamoDB, config);
    }

    @Bean
    @Primary
    public AmazonDynamoDB amazonDynamoDB() {
        return amazonDynamoDB;
    }
}
