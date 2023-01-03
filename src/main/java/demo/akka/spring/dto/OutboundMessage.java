package demo.akka.spring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class OutboundMessage {

    private String id;
    private String message;
    private String sourceId;
}
