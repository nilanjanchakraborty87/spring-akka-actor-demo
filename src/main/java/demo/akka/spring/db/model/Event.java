package demo.akka.spring.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event implements Persistable<String> {

    @Id
    private String id;
    private String schema_class;
    private byte[] message;

    @Transient
    private boolean isNew;

}
