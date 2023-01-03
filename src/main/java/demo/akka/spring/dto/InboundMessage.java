package demo.akka.spring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * a generic message format. Any app wants to publish their events to DemoApp, has to follow
 * this format. Other apps can set their message in the <b>body</b> of the DemoAppEvent.
 *
 * Along with the message, every app will need to set an unique message indentifier and
 * also some standard headers.
 *
 */
@Builder
@ToString
@Getter
public class InboundMessage<T> {
    private String id;
    private InboundMessageHeaders headers;
    private byte[] body;
    private long index;
}
