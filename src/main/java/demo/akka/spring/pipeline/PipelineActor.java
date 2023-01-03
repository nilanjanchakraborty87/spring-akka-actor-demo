package demo.akka.spring.pipeline;

import akka.actor.AbstractActor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.akka.spring.app1.BookInfo;
import demo.akka.spring.db.model.Event;
import demo.akka.spring.db.model.EventOutMapping;
import demo.akka.spring.db.repo.EventOutMappingRepository;
import demo.akka.spring.db.repo.EventRepository;
import demo.akka.spring.dto.InboundMessage;
import demo.akka.spring.dto.OutboundMessage;
import io.nats.client.api.PublishAck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
@Component("PipelineActor")
@Scope("prototype")
public class PipelineActor extends AbstractActor {

    private static final String APPLICATION_JSON = "application/json";

    private Pipeline pipeline;

    @Autowired
    private TransactionalOperator txOp;

    @Autowired
    private PipelineACKPubliher ackPubliher;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventOutMappingRepository eventOutMappingRepository;

    private ObjectMapper jsonMapper = new ObjectMapper();

    public PipelineActor(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    private void onInboundMessage(InboundMessage in) {
        final long start = System.nanoTime();
        log.info("received message in actor with index - {}", in.getIndex());
        //null checks are avoided intentionally to save coding time.

        //event processing
        var eventProcessing = Mono.fromFuture(() -> {
                //deserialize the message
                var bookInfo = deserialize(in.getBody(), in.getHeaders().getSchemaClazz());

                //call the processor
                return pipeline.getProcessor().process(in.getId(), bookInfo)
                        .thenApply(out -> demo.akka.spring.dto.Tuple2.of(bookInfo, out));

        });

        // store event-out object mapping
        BiFunction<String, OutboundMessage, Mono<EventOutMapping>> eventOutMappingFn = (eventId, out ) -> {
            try {
                EventOutMapping mapping = new EventOutMapping();
                mapping.setId(UUID.randomUUID().toString());
                mapping.setEventId(eventId);
                mapping.setNew(true);
                mapping.setOutMessage(jsonMapper.writeValueAsBytes(out));
                return eventOutMappingRepository.save( mapping );
            } catch (JsonProcessingException ex) {
                return Mono.error(ex);
            }
        };

        //update the main event and store the message parallelly
        Function<Tuple2<Event, demo.akka.spring.dto.Tuple2>, Mono<PublishAck>> updateMappingFn = t2 ->
                eventOutMappingFn.apply( t2.getT1().getId(), (OutboundMessage) t2.getT2().b )
                //publish the message acknowledgement
                .then(Mono.fromFuture(ackPubliher.publishAck(in)))
                // mark this as transactional
                .as( txOp::transactional );

        Event event = new Event();
        event.setId(in.getId());
        event.setNew(true);
        event.setSchema_class(in.getHeaders().getSchemaClazz().getCanonicalName());
        event.setMessage(in.getBody());

        //wait for below two mono to complete and then call the message update
        Mono.zipDelayError(eventRepository.save(event), eventProcessing)
                .flatMap( updateMappingFn )
                .doOnNext(i -> log.info("Time taken : " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds"))
                .doOnError(ex -> log.error("failed to store primary event or process the message", ex))
                //not much performance gain. Mostly these threads are getting parked
                //.subscribeOn(reactorScheduler)
                .subscribe();

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InboundMessage.class, this::onInboundMessage)
                .matchAny(message -> System.out.println("Received unknown message: " + message))
                .build();
    }

    private BookInfo deserialize(byte[] body, Class clazz) {
        try {
            return (BookInfo)jsonMapper.readValue(body, clazz);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
