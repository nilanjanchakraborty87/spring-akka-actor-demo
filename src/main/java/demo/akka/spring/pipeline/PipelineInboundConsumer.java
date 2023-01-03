package demo.akka.spring.pipeline;

import akka.actor.ActorRef;
import demo.akka.spring.dto.InboundMessage;
import demo.akka.spring.msg.ext.InboundChannelListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A consumer which receives message from a pipeline inbound channel. On receive, it sends the message
 * to PipelineActor.
 *
 * This can be any message consumer like kafka, NATS consumer etc. For this demo implementation, this will be a observer
 * which will receive message in it's callback in a certain interval
 */
@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class PipelineInboundConsumer implements InboundChannelListener {

    private final ActorRef pipelineRouter;
    private final String inboundChannel;

    /**
     * initialize the consumer. In this stage, consumer establishes a connection to
     * the actual message provider.
     *
     * for this demo implementation, it won't need anything.
     */
    public void init() {
        log.info("pipeline inbound message consumer initialised for inbound channel - {}", inboundChannel);
    }

    @Override
    public void onMessage(InboundMessage msg) {
        pipelineRouter.tell(msg, ActorRef.noSender());
    }
}
