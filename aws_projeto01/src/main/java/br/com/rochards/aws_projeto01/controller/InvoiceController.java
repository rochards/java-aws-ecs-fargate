package br.com.rochards.aws_projeto01.controller;

import br.com.rochards.aws_projeto01.dto.UrlResponse;
import br.com.rochards.aws_projeto01.model.Invoice;
import br.com.rochards.aws_projeto01.repository.InvoiceRepository;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/invoices")
public class InvoiceController {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceController.class);

    @Value("${aws.s3.bucket.invoice.name}")
    private String bucketName;

    private final AmazonS3 amazonS3;
    private final InvoiceRepository repository;

    @Autowired
    public InvoiceController(AmazonS3 amazonS3, InvoiceRepository repository) {
        this.amazonS3 = amazonS3;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createInvoiceUrl() {
        var expirationTime = Instant.now().plus(Duration.ofMinutes(5));
        var processId = UUID.randomUUID().toString();

        var presignedUrl = new GeneratePresignedUrlRequest(bucketName, processId)
                .withMethod(HttpMethod.PUT)
                .withExpiration(Date.from(expirationTime));

        var urlResponse = new UrlResponse(
                amazonS3.generatePresignedUrl(presignedUrl).toString(),
                expirationTime.getEpochSecond()
        );

        LOG.info("Retornando URl pre-assinada do bucket");
        return ResponseEntity.ok(urlResponse);
    }

    @GetMapping
    public Iterable<Invoice> findAll() {
        return repository.findAll();
    }

    @GetMapping(params = "bycustomername")
    public List<Invoice> findByCustomerName(String customerName) {
        LOG.info("bycustomername = {}", customerName);
        return repository.findAllByCustomerName(customerName);
    }
}
