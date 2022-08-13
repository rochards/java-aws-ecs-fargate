package br.com.rochards.aws_projeto01.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@ToString
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 32, nullable = false, unique = true)
    private String invoiceNumber;

    @Column(length = 32, nullable = false)
    private String customerName;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalValue;

    private long productId;
    private int quantity;
}
