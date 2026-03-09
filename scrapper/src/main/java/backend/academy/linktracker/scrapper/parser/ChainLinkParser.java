package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ChainLinkParser implements LinkParser {
    private final List<LinkParser> parsers;

    public ChainLinkParser(GitHubLinkParser gitHubParser, StackOverflowLinkParser stackOverflowParser) {
        this.parsers = List.of(gitHubParser, stackOverflowParser);
    }

    @Override
    public Optional<ParsedLink> parse(URI url) {
        return parsers.stream()
                .map(parser -> parser.parse(url))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
