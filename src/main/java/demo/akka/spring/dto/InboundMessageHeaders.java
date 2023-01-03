package demo.akka.spring.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InboundMessageHeaders {

    private String title;
    private String description;
    private Class schemaClazz;
    private String purpose;
    private String contentType;
}
