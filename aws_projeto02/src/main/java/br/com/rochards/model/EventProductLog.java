package br.com.rochards.model;

import br.com.rochards.enums.EventType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@DynamoDBTable(tableName = "product-events")
public class EventProductLog {

    @Id
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private EventProductKey eventProductKey;

    @DynamoDBTypeConvertedEnum
    private EventType eventType;

    private long productId;
    private String username;
    private long timestamp;
    private long ttl;

    public EventProductLog(String partitionKey, String sortKey, long productId, String username) {
        var instant = Instant.now();

        setPartitionKey(partitionKey);
        setSortKey(sortKey + "_" + instant.toEpochMilli());

        this.productId = productId;
        this.username = username;
        this.timestamp = instant.toEpochMilli();
        this.ttl = instant.plus(Duration.ofMinutes(10)).getEpochSecond();
    }

    @DynamoDBHashKey(attributeName = "pk")
    public String getPartitionKey() {
        return Objects.nonNull(eventProductKey) ? eventProductKey.getPartitionKey() : null;
    }

    public void setPartitionKey(String pk) {
        if (Objects.isNull(eventProductKey)) {
            this.eventProductKey = new EventProductKey();
        }
        this.eventProductKey.setPartitionKey(pk);
    }

    @DynamoDBRangeKey(attributeName = "sk")
    public String getSortKey() {
        return Objects.nonNull(eventProductKey) ? eventProductKey.getSortKey() : null;
    }

    public void setSortKey(String sk) {
        if (Objects.isNull(eventProductKey)) {
            this.eventProductKey = new EventProductKey();
        }
        this.eventProductKey.setSortKey(sk);
    }
}
