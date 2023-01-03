package demo.akka.spring.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("event_out_mapping")
public class EventOutMapping implements Persistable<String> {

    @Id
    private String id;
    private String eventId;
    private byte[] outMessage;

    @Transient
    private boolean isNew;

}
