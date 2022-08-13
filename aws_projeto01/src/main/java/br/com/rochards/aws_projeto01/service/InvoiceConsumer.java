package br.com.rochards.aws_projeto01.service;

import br.com.rochards.aws_projeto01.dto.SnsMessage;
import br.com.rochards.aws_projeto01.model.Invoice;
import br.com.rochards.aws_projeto01.repository.InvoiceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class InvoiceConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceConsumer.class);

    private final AmazonS3 s3Client;
    private final ObjectMapper mapper;
    private final InvoiceRepository repository;

    @Autowired
    public InvoiceConsumer(AmazonS3 s3Client, ObjectMapper mapper, InvoiceRepository repository) {
        this.s3Client = s3Client;
        this.mapper = mapper;
        this.repository = repository;
    }

    @JmsListener(destination = "${aws.sqs.queue.invoice.events.name}")
    public void receiveS3Event(TextMessage textMessage) throws JMSException, IOException {
        LOG.info("Evento da fila de invoices recebido");

        var snsMessage = mapper.readValue(textMessage.getText(), SnsMessage.class);
        var s3EventNotification = mapper.readValue(snsMessage.getMessage(), S3EventNotification.class);

        LOG.info("s3EventoNotification recebido: {}", s3EventNotification.toJson());

        processInvoiceNotification(s3EventNotification);
    }

    private void processInvoiceNotification(S3EventNotification s3EventNotification) throws IOException {
        for (var s3EventNotificationRecord : s3EventNotification.getRecords()) {
            var s3Entity = s3EventNotificationRecord.getS3();
            var bucketName = s3Entity.getBucket().getName();
            var objectKey = s3Entity.getObject().getKey();

            LOG.info("Fazendo download da invoice: {} no bucket: {}", objectKey, bucketName);
            var invoiceFile = downloadObject(bucketName, objectKey);
            LOG.info("Download da invoice com sucesso: {}", invoiceFile);

            // novamente, seria mais interessante se construisse um DTO
            var invoice = mapper.readValue(invoiceFile, Invoice.class);

            LOG.info("Salvando invoice {} no banco de dados", invoice);
            repository.save(invoice);
            LOG.info("Invoice salva com sucesso");
        }
    }

    private String downloadObject(String bucketName, String objectKey) throws IOException {
        var s3Object = s3Client.getObject(bucketName, objectKey);

        var bufferedReader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
        var sb = new StringBuilder();
        String content = null;
        while ((content = bufferedReader.readLine()) != null) {
            sb.append(content);
        }
        return sb.toString();
    }
}
