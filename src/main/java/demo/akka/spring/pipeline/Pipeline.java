package demo.akka.spring.pipeline;

import akka.actor.ActorRef;
import demo.akka.spring.app1.BookProcessor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Pipeline  {

    private final String name;
    private final String description;
    private final String inboundChannel;
    private final PipelineProcessor processor;
    private final String outboundChannel;
    private final String errorChannel;
    private ActorRef inboundMsgRouter;

}
