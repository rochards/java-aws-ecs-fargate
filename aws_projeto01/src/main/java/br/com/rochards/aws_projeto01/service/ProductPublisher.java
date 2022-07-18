package br.com.rochards.aws_projeto01.service;

import br.com.rochards.aws_projeto01.enums.EventType;
import br.com.rochards.aws_projeto01.model.EnvelopeEventProduct;
import br.com.rochards.aws_projeto01.model.Produto;
import br.com.rochards.aws_projeto01.model.EventProduct;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPublisher.class);

    private final AmazonSNS snsClient;
    private final ObjectMapper mapper;
    private final Topic topicEventProduct;

    @Autowired
    public ProductPublisher(AmazonSNS snsClient, ObjectMapper mapper, Topic topicEventProduct) {
        this.snsClient = snsClient;
        this.mapper = mapper;
        this.topicEventProduct = topicEventProduct;
    }

    public void publishEventProduct(Produto produto, EventType eventType, String username) {
        var eventProduct = new EventProduct(produto.getId(), produto.getCodigo(), username);
        var envelope = new EnvelopeEventProduct(eventType, eventProduct);
        var optEnvelopeString = toJsonString(envelope);

        optEnvelopeString.ifPresentOrElse(
                envelopeString -> snsClient.publish(topicEventProduct.getTopicArn(), envelopeString),
                () -> LOG.error("Falha ao publicar evento de produto")
        );
    }

    private Optional<String> toJsonString(EnvelopeEventProduct envelope) {
        try {
            return Optional.ofNullable(mapper.writeValueAsString(envelope));
        } catch (JsonProcessingException e) {
            LOG.error("Erro ao serializar EnvelopeProdutoEvento ", e);
            return Optional.empty();
        }
    }
}
