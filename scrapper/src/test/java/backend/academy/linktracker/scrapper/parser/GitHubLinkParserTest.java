package backend.academy.linktracker.scrapper.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;

class GitHubLinkParserTest {
    private final GitHubLinkParser parser = new GitHubLinkParser(null);

    @Test
    void parse_ShouldReturnGitHubParsedLink_WhenValidRepoUrl() {
        URI url = URI.create("https://github.com/spring-projects/spring-boot");

        ParsedLink result = parser.parse(url);

        GitHubParsedLink parsed = assertInstanceOf(GitHubParsedLink.class, result);
        assertEquals("spring-projects", parsed.owner());
        assertEquals("spring-boot", parsed.repo());
    }

    @Test
    void parse_ShouldReturnGitHubParsedLink_WhenUrlHasExtraPath() {
        URI url = URI.create("https://github.com/user/repo/tree/main/src");

        ParsedLink result = parser.parse(url);

        GitHubParsedLink parsed = assertInstanceOf(GitHubParsedLink.class, result);
        assertEquals("user", parsed.owner());
        assertEquals("repo", parsed.repo());
    }

    @Test
    void supports_ShouldReturnFalse_WhenNotGitHubHost() {
        URI url = URI.create("https://gitlab.com/user/repo");

        assertFalse(parser.supports(url));
    }

    @Test
    void supports_ShouldReturnTrue_WhenGitHubHost() {
        URI url = URI.create("https://github.com/user/repo");

        assertTrue(parser.supports(url));
    }

    @Test
    void parse_ShouldThrow_WhenPathTooShort() {
        URI url = URI.create("https://github.com/user");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(url));
    }
}
