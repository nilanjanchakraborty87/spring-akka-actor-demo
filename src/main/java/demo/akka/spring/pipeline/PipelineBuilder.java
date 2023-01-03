package demo.akka.spring.pipeline;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.DefaultResizer;
import akka.routing.RoundRobinPool;
import demo.akka.spring.app1.BookProcessor;
import demo.akka.spring.msg.ext.ScheduledInboundMessageGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;

import static demo.akka.spring.config.AkkaSpringExt.AkkaSpringExtProvider;

@RequiredArgsConstructor
public class PipelineBuilder {

    private final ActorSystem actorSystem;
    private final ApplicationContext applicationContext;

    private void buildPipeline() {
        DefaultResizer resizer = new DefaultResizer(5, 10);

        var pipeline =
                Pipeline.builder()
                        .name("App1Pipeline")
                        .inboundChannel("app1Channel")
                        .processor(applicationContext.getBean(BookProcessor.class))
                        .build();

        //MessageDispatcher lookup = actorSystem.dispatchers().lookup("fixed-thread-pool");
        ActorRef router = actorSystem.actorOf(new RoundRobinPool(100)
                        .withResizer(resizer)
                        //.withDispatcher("fixed-thread-pool")
                        .props(AkkaSpringExtProvider.get(actorSystem).props("PipelineActor", pipeline)),
                "PipelineMessageRouter"
        );

        PipelineInboundConsumer consumer = applicationContext.getBean(PipelineInboundConsumer.class, router, pipeline.getInboundChannel());
        consumer.init();

        var generator = applicationContext.getBean(ScheduledInboundMessageGenerator.class);
        generator.registerListener(consumer);

    }
}
