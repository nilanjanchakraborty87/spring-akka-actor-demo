package demo.akka.spring.db.repo;

import demo.akka.spring.db.model.Event;
import demo.akka.spring.db.model.EventOutMapping;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveCrudRepository<Event, String> {
}
