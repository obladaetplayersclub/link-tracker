package backend.academy.linktracker.scrapper.parser;

import java.net.URI;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkParser implements LinkParser {

    @Override
    public Optional<ParsedLink> parse(URI url) {
        if (url.getHost() == null || !url.getHost().equals("stackoverflow.com")) {
            return Optional.empty();
        }
        String[] segments = url.getPath().split("/");
        if (segments.length < 3 || !"questions".equals(segments[1])) {
            return Optional.empty();
        }
        try {
            long questionId = Long.parseLong(segments[2]);
            return Optional.of(new StackOverflowParsedLink(questionId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
