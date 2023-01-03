package demo.akka.spring.dto;

import lombok.*;

@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ACKEvent {

    public String message;
    private String sourceId;
    private String ID;

}
