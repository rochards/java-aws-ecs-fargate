package br.com.rochards.controller;

import br.com.rochards.dto.EventProductLogDto;
import br.com.rochards.repository.EventProductLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class EventProductLogController {

    private static final Logger LOG = LoggerFactory.getLogger(EventProductLogController.class);

    private final EventProductLogRepository repository;

    @Autowired
    public EventProductLogController(EventProductLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/eventos")
    public List<EventProductLogDto> findByCode(@RequestParam("codigoProduto") String code,
                                               @RequestParam(value = "tipoEvento", required = false) String eventType) {

        LOG.info("Consultando eventos no DynamoDB. codigoProduto={}, tipoEvento={}", code, eventType);

        var eventProductLogList = Objects.isNull(eventType) ?
                repository.findAllByPk(code) : repository.findAllByPkAndSkStartsWith(code, eventType);

        LOG.info("Eventos encontrados: {}", eventProductLogList.size());

        return eventProductLogList
                .stream()
                .map(EventProductLogDto::new)
                .collect(Collectors.toList());
    }
}
