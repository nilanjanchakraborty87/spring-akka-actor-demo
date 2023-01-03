package demo.akka.spring.msg.ext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.akka.spring.app1.BookInfo;
import demo.akka.spring.dto.InboundMessage;
import demo.akka.spring.dto.InboundMessageHeaders;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;

@Slf4j
@Component
public class ScheduledInboundMessageGenerator {

    private static final String APPLICATION_JSON = "application/json";

    private StopWatch sw = new StopWatch();

    AtomicLong counter = new AtomicLong();
    ObjectMapper jsonMapper = new ObjectMapper();
    Faker faker = new Faker();
    public List<InboundChannelListener> listeners = new ArrayList<>();
    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public void registerListener(InboundChannelListener listener) {
        this.listeners.add(listener);
    }

    private Function<Long, InboundMessage> dataFunc = (index) -> {
        var book = BookInfo.builder().name(faker.book().title())
                .author(faker.book().author())
                .isbn(faker.code().isbn10())
                .genre(faker.book().genre())
                .build();

        try {
            InboundMessage<BookInfo> event = InboundMessage.<BookInfo>builder()
                    .id(UUID.randomUUID().toString())
                    .body(jsonMapper.writeValueAsBytes(book))
                    .headers(InboundMessageHeaders.builder().schemaClazz(BookInfo.class).contentType(APPLICATION_JSON).build())
                    .index(index)
                    .build();

            return event;
        } catch (JsonProcessingException e) {
           return null;
        }
    };

    @Scheduled(fixedRate = 6000000, initialDelay = 5000)
    public void generateEvent() throws Exception {
        Runnable task = () -> {
            var index = counter.incrementAndGet();
            var o = dataFunc.apply(index);
            log.info("Published message index - {}", index);
            listeners.get(0).onMessage(o);
        };

        IntStream.range(0, 100000).forEach((i) -> {
            executor.submit(task);
        });
    }

    @PostConstruct
    private void init() {
        sw.start();
    }

    @PreDestroy
    private void destroy() {
        sw.stop();
        System.out.println("Total time took " + sw.getTotalTimeMillis());
        System.out.println("End time - " + System.currentTimeMillis());
    }

}
