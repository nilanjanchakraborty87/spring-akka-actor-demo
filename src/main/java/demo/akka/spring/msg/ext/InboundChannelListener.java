package demo.akka.spring.msg.ext;

import demo.akka.spring.dto.InboundMessage;

public interface InboundChannelListener {

    void onMessage(InboundMessage event);
}
