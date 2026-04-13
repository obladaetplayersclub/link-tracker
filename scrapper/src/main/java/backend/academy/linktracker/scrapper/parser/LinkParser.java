package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkParser {
    boolean supports(URI url);

    ParsedLink parse(URI url);

    List<LinkUpdateInfo> checkUpdates(URI url, OffsetDateTime since);
}
