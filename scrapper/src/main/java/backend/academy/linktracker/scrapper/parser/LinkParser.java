package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.util.Optional;

public interface LinkParser {
    Optional<ParsedLink> parse(URI url);
}
