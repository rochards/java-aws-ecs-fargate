package br.com.rochards.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EventProductKey {

    @DynamoDBHashKey(attributeName = "pk")
    private String partitionKey;

    @DynamoDBRangeKey(attributeName = "sk")
    private String sortKey;
}
