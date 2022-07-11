package br.com.rochards.aws_projeto01.repository;


import br.com.rochards.aws_projeto01.model.Produto;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProdutoRepository extends CrudRepository<Produto, Long> {

    Optional<Produto> findByCodigo(String codigo);
}
