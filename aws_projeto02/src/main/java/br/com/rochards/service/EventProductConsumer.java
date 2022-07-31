package br.com.rochards.service;

import br.com.rochards.model.EnvelopeEventProduct;
import br.com.rochards.model.EventProduct;
import br.com.rochards.model.SnsMessage;
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

    @Autowired
    public EventProductConsumer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @JmsListener(destination = "${aws.sqs.product.events.queue.name}")
    public void receiveEventProduct(TextMessage textMessage) throws JMSException, IOException {
        var snsMessage = mapper.readValue(textMessage.getText(), SnsMessage.class);
        var envelopeEventProduct = mapper.readValue(snsMessage.getMessage(), EnvelopeEventProduct.class);

        LOG.info("Evento de produto recebido - Evento: {}, ID do produto: {}",
                envelopeEventProduct.getEventType(), envelopeEventProduct.getData().getId());
    }
}
