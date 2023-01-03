package demo.akka.spring.config;

import akka.actor.ActorSystem;
import demo.akka.spring.pipeline.PipelineBuilder;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static demo.akka.spring.config.AkkaSpringExt.AkkaSpringExtProvider;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    // the application context is needed to initialize the Akka Spring Extension
    private final ApplicationContext applicationContext;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("demo-app-actor-system");
        // initialize the application context in the Akka Spring Extension
        AkkaSpringExtProvider.get(system).initialize(applicationContext);
        return system;
    }

    @Bean(initMethod = "buildPipeline")
    public PipelineBuilder pipelineBuilder(ActorSystem actorSystem) {
        return new PipelineBuilder(actorSystem, applicationContext);
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public Scheduler reactorScheduler () {
        return Schedulers.newBoundedElastic(50, 100, "reactor-scheduler");
    }

    @PreDestroy
    private void onExit() {
        System.out.println("closing actor system");
        ActorSystem actorSystem = applicationContext.getBean(ActorSystem.class);
        actorSystem.terminate();
    }

}
