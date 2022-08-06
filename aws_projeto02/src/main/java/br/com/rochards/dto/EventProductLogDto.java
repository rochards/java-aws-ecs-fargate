package br.com.rochards.dto;

import br.com.rochards.enums.EventType;
import br.com.rochards.model.EventProductLog;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
public class EventProductLogDto {

    @JsonProperty("codigoProduto")
    private final String code;
    @JsonProperty("tipoEvento")
    private final EventType eventType;
    @JsonProperty("idProduto")
    private final long productId;
    private final String username;
    @JsonProperty("criadoEm")
    private LocalDateTime timestamp;

    public EventProductLogDto(EventProductLog eventProductLog) {
        this.code = eventProductLog.getPk();
        this.eventType = eventProductLog.getEventType();
        this.productId = eventProductLog.getProductId();
        this.username = eventProductLog.getUsername();
        setTimestamp(eventProductLog.getTimestamp());
    }

    private void setTimestamp(long timestamp) {
        this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}
