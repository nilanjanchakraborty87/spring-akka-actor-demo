package demo.akka.spring.pipeline;

import demo.akka.spring.app1.IBody;
import demo.akka.spring.dto.OutboundMessage;

import java.util.concurrent.CompletableFuture;

public interface PipelineProcessor {

   <T> CompletableFuture<OutboundMessage> process(String msgId, IBody msg);

}
