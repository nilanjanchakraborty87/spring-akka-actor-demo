package demo.akka.spring.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.akka.spring.dto.ACKEvent;
import demo.akka.spring.dto.InboundMessage;
import io.nats.client.*;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.nats.client.impl.NatsMessage;
import io.nats.client.support.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * publishes the ACK/ERR events to local NATS server
 */
@Slf4j
@Service
public class PipelineACKPubliher {

    @Value("${nats.server:nats://localhost:4222}")
    private String natsServer;

    @Value("${nats.stream:akka-demo}")
    private String natsStream;

    @Value("${nats.subject:akka-demo-ack-events}")
    private String natsSubject;
    private ObjectMapper jsonMapper = new ObjectMapper();
    private Connection nc;
    private JetStream js;

    private ExecutorService natsAckPublishPool = Executors.newFixedThreadPool(20);;

    @PostConstruct
    private void init() {
        try {
            nc = Nats.connect(natsServer);

            JetStreamManagement jsm = nc.jetStreamManagement();

            // Create a stream, here will use an in-memory storage type, and one subject
            StreamConfiguration sc = StreamConfiguration.builder()
                    .name(natsStream)
                    .storageType(StorageType.Memory)
                    .subjects(natsSubject)
                    .build();

            // Add a stream.
            StreamInfo streamInfo = jsm.addStream(sc);
            JsonUtils.printFormatted(streamInfo);

            // Create a JetStream context.  This hangs off the original connection
            // allowing us to produce data to streams and consume data from
            // JetStream consumers.
            this.js = nc.jetStream();
        }
        catch( Exception ex ){
            throw new RuntimeException("unable to connect to NATS server", ex);
        }
    }

    public CompletableFuture<PublishAck> publishAck(InboundMessage i) {
        var appAck = ACKEvent.of("Acknowledgement for received message", i.getId(), UUID.randomUUID().toString());
        try {
            Message msg = NatsMessage.builder()
                    .subject(natsSubject)
                    .data(jsonMapper.writeValueAsString(appAck), StandardCharsets.UTF_8)
                    .build();

            return js.publishAsync(msg).thenApplyAsync(ack -> {
                log.info("ACK Info - {}", ack);
                return ack;
            }, natsAckPublishPool);
        }
        catch( Exception ex ) {
            return CompletableFuture.failedFuture(ex);
        }
    }

}
