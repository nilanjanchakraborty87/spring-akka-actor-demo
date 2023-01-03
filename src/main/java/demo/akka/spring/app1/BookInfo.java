package demo.akka.spring.app1;

import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookInfo implements IBody {

    private String author;
    private String name;
    private String genre;
    private String isbn;
}
