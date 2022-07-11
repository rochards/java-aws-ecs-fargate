package br.com.rochards.aws_projeto01.controller;

import br.com.rochards.aws_projeto01.model.Produto;
import br.com.rochards.aws_projeto01.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;

    @Autowired
    private ProdutoController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Iterable<Produto> buscaTodos() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscaPorId(@PathVariable long id) {
        var optProduto = repository.findById(id);
        return optProduto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Produto> buscaPorCodigo(@PathVariable String codigo) {
        var optProduto = repository.findByCodigo(codigo);
        return optProduto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // o recomendado abaixo seria receber uma classe DTO em vez de ser o objeto entidade da JPA
    @PostMapping
    public ResponseEntity<?> salvaProduto(@RequestBody Produto produto, UriComponentsBuilder uriBuilder) {
        var produtoSalvo = repository.save(produto);
        var location = uriBuilder.path("/api/v1/produtos/{id}").build(produtoSalvo.getId());
        return ResponseEntity.created(location).build();
    }

    // o recomendado abaixo seria receber uma classe DTO em vez de ser o objeto entidade da JPA
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizaProduto(@RequestBody Produto produto, @PathVariable long id) {
        if (repository.existsById(id)) {
            produto.setId(id);
            repository.save(produto);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Produto> exclui(@PathVariable long id) {
        var optProduto = repository.findById(id);
        return optProduto.map(produto -> {
            repository.delete(produto);
            return ResponseEntity.ok(produto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
