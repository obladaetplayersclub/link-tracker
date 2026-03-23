package backend.academy.linktracker.scrapper.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class StackOverflowLinkParserTest {
    private final StackOverflowLinkParser parser = new StackOverflowLinkParser(null, null);

    @Test
    void parse_ShouldReturnStackOverflowParsedLink_WhenValidQuestionUrl() {
        URI url = URI.create("https://stackoverflow.com/questions/12345/some-question-title");

        ParsedLink result = parser.parse(url);

        StackOverflowParsedLink parsed = assertInstanceOf(StackOverflowParsedLink.class, result);
        assertEquals(12345L, parsed.questionId());
    }

    @Test
    void supports_ShouldReturnFalse_WhenNotStackOverflowHost() {
        URI url = URI.create("https://superuser.com/questions/12345/title");

        assertFalse(parser.supports(url));
    }

    @Test
    void supports_ShouldReturnTrue_WhenStackOverflowHost() {
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");

        assertTrue(parser.supports(url));
    }

    @Test
    void parse_ShouldThrow_WhenPathIsNotQuestion() {
        URI url = URI.create("https://stackoverflow.com/users/12345/username");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(url));
    }

    @Test
    void parse_ShouldThrow_WhenQuestionIdIsNotANumber() {
        URI url = URI.create("https://stackoverflow.com/questions/abc/title");

        assertThrows(NumberFormatException.class, () -> parser.parse(url));
    }
}
