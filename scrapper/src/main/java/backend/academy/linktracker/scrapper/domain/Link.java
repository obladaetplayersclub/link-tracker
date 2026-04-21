package backend.academy.linktracker.scrapper.domain;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Link {
    private Long id;
    private URI url;
    private List<String> tags;
    private OffsetDateTime lastUpdated;
}
