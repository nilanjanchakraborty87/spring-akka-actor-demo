package demo.akka.spring.app1;

import demo.akka.spring.dto.OutboundMessage;
import demo.akka.spring.pipeline.PipelineProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@Scope("singleton")
@RequiredArgsConstructor
public class BookProcessor implements PipelineProcessor {

    private String[] apis = {
            "https://catfact.ninja/fact",
            "https://api.coindesk.com/v1/bpi/currentprice.json", //fastest api, average response time below <50ms
    "https://www.boredapi.com/api/activity",
            "https://api.agify.io/?name=nilanjan",
            "https://api.genderize.io/?name=luc",
            "https://api.nationalize.io/?name=india"
    };
    private HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newCachedThreadPool())
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private Random rnd = new Random();
    private final ExecutorService resExecutorService = Executors.newFixedThreadPool(5);

    @Override
    public CompletableFuture<OutboundMessage> process(String msgId, IBody body) {
        BookInfo book = (BookInfo) body;
        /*return CompletableFuture.supplyAsync(() -> {
            StringBuilder builder = new StringBuilder("Received a book info");
            builder.append("\n--------------------\n");
            builder.append("---Title - ").append(book.getName()).append("-------\n");
            builder.append("---Isbn - ").append(book.getIsbn()).append("-------\n");
            builder.append("---Author - ").append(book.getAuthor()).append("-------\n");
            builder.append("---Genre - ").append(book.getGenre()).append("-------\n");
            builder.append("--------------------\n");
            return OutboundMessage.builder()
                    .id(UUID.randomUUID().toString().replaceAll("-", ""))
                    .message(builder.toString())
                    .sourceId(msgId)
                    .build();
        })*/
        final String uri = "https://api.coindesk.com/v1/bpi/currentprice.json";
        var httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .build();

        return httpClient
                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(r -> {
                    //log.info(r.body());

                    StringBuilder builder = new StringBuilder("Received a book info");
                    builder.append("\n--------------------\n");
                    builder.append("---Title - ").append(book.getName()).append("-------\n");
                    builder.append("---Isbn - ").append(book.getIsbn()).append("-------\n");
                    builder.append("---Author - ").append(book.getAuthor()).append("-------\n");
                    builder.append("---Genre - ").append(book.getGenre()).append("-------\n");
                    builder.append("--------------------\n");
                    return OutboundMessage.builder()
                            .id(UUID.randomUUID().toString().replaceAll("-", ""))
                            .message(builder.toString())
                            .sourceId(msgId)
                            .build();
                }, resExecutorService);

    }
}
