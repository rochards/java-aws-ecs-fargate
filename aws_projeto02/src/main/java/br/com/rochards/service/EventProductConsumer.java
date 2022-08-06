package br.com.rochards.service;

import br.com.rochards.dto.EnvelopeEventProduct;
import br.com.rochards.dto.EventProduct;
import br.com.rochards.dto.SnsMessage;
import br.com.rochards.model.EventProductLog;
import br.com.rochards.repository.EventProductLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;

@Service
public class EventProductConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(EventProductConsumer.class);

    private final ObjectMapper mapper;
    private final EventProductLogRepository repository;

    @Autowired
    public EventProductConsumer(ObjectMapper mapper, EventProductLogRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @JmsListener(destination = "${aws.sqs.product.events.queue.name}")
    public void receiveEventProduct(TextMessage textMessage) throws JMSException, IOException {
        var snsMessage = mapper.readValue(textMessage.getText(), SnsMessage.class);
        var envelopeEventProduct = mapper.readValue(snsMessage.getMessage(), EnvelopeEventProduct.class);
        var eventProduct = envelopeEventProduct.getData();

        LOG.info("ID da mensagem={}. Evento de produto recebido - Evento: {}, ID do produto: {}",
                snsMessage.getMessageId(), envelopeEventProduct.getEventType(), eventProduct.getId());

        saveEventProduct(eventProduct, envelopeEventProduct.getEventType().name());
    }

    private void saveEventProduct(EventProduct eventProduct, String eventType) {
        var eventProductLog =
                new EventProductLog(eventProduct.getCodigo(), eventType, eventProduct.getId(), eventProduct.getUsername());

        LOG.info("Salvando log de produtos no DynamoDB: {} ", eventProduct.toString());

        repository.save(eventProductLog);

        LOG.info("Log salvo com sucesso!");
    }
}
