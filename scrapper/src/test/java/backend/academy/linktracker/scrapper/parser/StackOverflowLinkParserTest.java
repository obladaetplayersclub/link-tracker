package backend.academy.linktracker.scrapper.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StackOverflowLinkParserTest {
    private final StackOverflowLinkParser parser = new StackOverflowLinkParser();

    @Test
    void parse_ShouldReturnStackOverflowParsedLink_WhenValidQuestionUrl() {
        URI url = URI.create("https://stackoverflow.com/questions/12345/some-question-title");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isPresent());
        StackOverflowParsedLink parsed = assertInstanceOf(StackOverflowParsedLink.class, result.orElseThrow());
        assertEquals(12345L, parsed.questionId());
    }

    @Test
    void parse_ShouldReturnEmpty_WhenNotStackOverflowHost() {
        URI url = URI.create("https://superuser.com/questions/12345/title");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isEmpty());
    }

    @Test
    void parse_ShouldReturnEmpty_WhenPathIsNotQuestion() {
        URI url = URI.create("https://stackoverflow.com/users/12345/username");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isEmpty());
    }

    @Test
    void parse_ShouldReturnEmpty_WhenQuestionIdIsNotANumber() {
        URI url = URI.create("https://stackoverflow.com/questions/abc/title");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isEmpty());
    }
}
