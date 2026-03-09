package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class GitHubLinkParser implements LinkParser {

    @Override
    public Optional<ParsedLink> parse(URI url) {
        if (url.getHost() == null || !url.getHost().equals("github.com")) {
            return Optional.empty();
        }
        String[] segments = url.getPath().split("/");
        if (segments.length < 3) {
            return Optional.empty();
        }
        return Optional.of(new GitHubParsedLink(segments[1], segments[2]));
    }
}
