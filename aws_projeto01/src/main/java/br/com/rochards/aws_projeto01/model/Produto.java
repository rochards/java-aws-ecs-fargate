package br.com.rochards.aws_projeto01.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Setter
@Getter
@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 32, nullable = false)
    private String nome;

    @Column(length = 8, nullable = false, unique = true)
    private String codigo;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;
}
