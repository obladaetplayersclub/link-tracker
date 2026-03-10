package backend.academy.linktracker.scrapper.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GitHubLinkParserTest {
    private final GitHubLinkParser parser = new GitHubLinkParser();

    @Test
    void parse_ShouldReturnGitHubParsedLink_WhenValidRepoUrl() {
        URI url = URI.create("https://github.com/spring-projects/spring-boot");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isPresent());
        GitHubParsedLink parsed = assertInstanceOf(GitHubParsedLink.class, result.orElseThrow());
        assertEquals("spring-projects", parsed.owner());
        assertEquals("spring-boot", parsed.repo());
    }

    @Test
    void parse_ShouldReturnGitHubParsedLink_WhenUrlHasExtraPath() {
        URI url = URI.create("https://github.com/user/repo/tree/main/src");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isPresent());
        GitHubParsedLink parsed = assertInstanceOf(GitHubParsedLink.class, result.orElseThrow());
        assertEquals("user", parsed.owner());
        assertEquals("repo", parsed.repo());
    }

    @Test
    void parse_ShouldReturnEmpty_WhenNotGitHubHost() {
        URI url = URI.create("https://gitlab.com/user/repo");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isEmpty());
    }

    @Test
    void parse_ShouldReturnEmpty_WhenPathTooShort() {
        URI url = URI.create("https://github.com/user");

        Optional<ParsedLink> result = parser.parse(url);

        assertTrue(result.isEmpty());
    }
}
