package backend.academy.linktracker.scrapper.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ChainLinkParserTest {
    private final ChainLinkParser parser = new ChainLinkParser(new GitHubLinkParser(), new StackOverflowLinkParser());

    @Test
    void parse_ShouldReturnGitHubParsedLink_WhenGitHubUrl() {
        URI url = URI.create("https://github.com/user/repo");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isPresent());
        assertInstanceOf(GitHubParsedLink.class, result.orElseThrow());
    }

    @Test
    void parse_ShouldReturnStackOverflowParsedLink_WhenStackOverflowUrl() {
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isPresent());
        assertInstanceOf(StackOverflowParsedLink.class, result.orElseThrow());
    }

    @Test
    void parse_ShouldReturnEmpty_WhenUnsupportedDomain() {
        URI url = URI.create("https://example.com/some/path");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isEmpty());
    }
}
