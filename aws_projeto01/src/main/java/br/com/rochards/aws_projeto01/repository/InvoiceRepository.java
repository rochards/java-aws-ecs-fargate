package br.com.rochards.aws_projeto01.repository;

import br.com.rochards.aws_projeto01.model.Invoice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAllByCustomerName(String customerName);
}
