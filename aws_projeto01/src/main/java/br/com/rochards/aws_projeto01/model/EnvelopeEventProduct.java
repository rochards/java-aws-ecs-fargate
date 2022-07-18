package br.com.rochards.aws_projeto01.model;

import br.com.rochards.aws_projeto01.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnvelopeEventProduct {

    private EventType eventType;
    private EventProduct data;
}
