package br.com.rochards.repository;

import br.com.rochards.model.EventProductKey;
import br.com.rochards.model.EventProductLog;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan // para a anotacao de Enable na classe DynamoDBConfig
public interface EventProductLogRepository extends CrudRepository<EventProductLog, EventProductKey> {

    // importante notar que Pk e Sk devem ser o nome do atributo (no case sensitive) lá na tabela do dynamodb
    List<EventProductLog> findAllByPk(String code); // important que os getters e setters tbm sejam nomeados pk e sk
    List<EventProductLog> findAllByPkAndSkStartsWith(String code, String eventType);
}
