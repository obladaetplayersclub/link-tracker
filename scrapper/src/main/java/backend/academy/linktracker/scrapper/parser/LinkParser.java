package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.time.OffsetDateTime;

public interface LinkParser {
    boolean supports(URI url);

    ParsedLink parse(URI url);

    OffsetDateTime checkUpdate(URI url);
}
