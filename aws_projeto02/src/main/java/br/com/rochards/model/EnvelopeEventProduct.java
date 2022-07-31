package br.com.rochards.model;

import br.com.rochards.enums.EventType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EnvelopeEventProduct {

    private EventType eventType;
    private EventProduct data;
}
