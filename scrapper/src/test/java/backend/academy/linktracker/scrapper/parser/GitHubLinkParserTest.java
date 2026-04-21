package backend.academy.linktracker.scrapper.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.dto.GitHub.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.client.dto.GitHub.GitHubUser;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitHubLinkParserTest {

    @Mock
    private GitHubClient gitHubClient;

    @InjectMocks
    private GitHubLinkParser parser;

    private static final OffsetDateTime SINCE = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime CREATED = OffsetDateTime.of(2025, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void parse_shouldReturnGitHubParsedLink_whenValidRepoUrl() {
        URI url = URI.create("https://github.com/spring-projects/spring-boot");

        ParsedLink result = parser.parse(url);

        GitHubParsedLink parsed = assertInstanceOf(GitHubParsedLink.class, result);
        assertThat(parsed.owner()).isEqualTo("spring-projects");
        assertThat(parsed.repo()).isEqualTo("spring-boot");
    }

    @Test
    void parse_shouldReturnGitHubParsedLink_whenUrlHasExtraPath() {
        URI url = URI.create("https://github.com/user/repo/tree/main/src");

        ParsedLink result = parser.parse(url);

        GitHubParsedLink parsed = assertInstanceOf(GitHubParsedLink.class, result);
        assertThat(parsed.owner()).isEqualTo("user");
        assertThat(parsed.repo()).isEqualTo("repo");
    }

    @Test
    void supports_shouldReturnFalse_whenNotGitHubHost() {
        assertFalse(parser.supports(URI.create("https://gitlab.com/user/repo")));
    }

    @Test
    void supports_shouldReturnTrue_whenGitHubHost() {
        assertTrue(parser.supports(URI.create("https://github.com/user/repo")));
    }

    @Test
    void parse_shouldThrow_whenPathTooShort() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(URI.create("https://github.com/user")));
    }

    @Test
    void checkUpdates_shouldReturnIssueInfo_whenNewIssueFound() {
        URI url = URI.create("https://github.com/owner/repo");
        when(gitHubClient.getIssues("owner", "repo", SINCE.toString()))
                .thenReturn(List.of(new GitHubRepoResponse.GitHubIssueResponse(
                        "Fix login bug", new GitHubUser("kostya"), CREATED, "Detailed description", null)));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).hasSize(1);
        assertThat(updates.getFirst().title()).isEqualTo("Issue: Fix login bug");
        assertThat(updates.getFirst().author()).isEqualTo("kostya");
        assertThat(updates.getFirst().preview()).isEqualTo("Detailed description");
    }

    @Test
    void checkUpdates_shouldReturnPRInfo_whenNewPullRequestFound() {
        URI url = URI.create("https://github.com/owner/repo");
        when(gitHubClient.getIssues("owner", "repo", SINCE.toString()))
                .thenReturn(List.of(new GitHubRepoResponse.GitHubIssueResponse(
                        "Add feature", new GitHubUser("dev"), CREATED, "New feature", new Object())));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).hasSize(1);
        assertThat(updates.getFirst().title()).isEqualTo("PR: Add feature");
    }

    @Test
    void checkUpdates_shouldTruncatePreview_whenBodyExceeds200Chars() {
        URI url = URI.create("https://github.com/owner/repo");
        String longBody = "A".repeat(300);
        when(gitHubClient.getIssues("owner", "repo", SINCE.toString()))
                .thenReturn(List.of(new GitHubRepoResponse.GitHubIssueResponse(
                        "Issue", new GitHubUser("user"), CREATED, longBody, null)));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates.getFirst().preview()).hasSize(200);
    }

    @Test
    void checkUpdates_shouldReturnEmptyList_whenNoUpdates() {
        URI url = URI.create("https://github.com/owner/repo");
        when(gitHubClient.getIssues("owner", "repo", SINCE.toString())).thenReturn(List.of());

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).isEmpty();
    }

    @Test
    void checkUpdates_shouldHandleNullBody() {
        URI url = URI.create("https://github.com/owner/repo");
        when(gitHubClient.getIssues("owner", "repo", SINCE.toString()))
                .thenReturn(List.of(new GitHubRepoResponse.GitHubIssueResponse(
                        "No body issue", new GitHubUser("user"), CREATED, null, null)));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates.getFirst().preview()).isEmpty();
    }

    @Test
    void checkUpdates_shouldReturnMultipleUpdates() {
        URI url = URI.create("https://github.com/owner/repo");
        when(gitHubClient.getIssues("owner", "repo", SINCE.toString()))
                .thenReturn(List.of(
                        new GitHubRepoResponse.GitHubIssueResponse(
                                "Issue 1", new GitHubUser("user1"), CREATED, "Body 1", null),
                        new GitHubRepoResponse.GitHubIssueResponse(
                                "PR 1", new GitHubUser("user2"), CREATED, "Body 2", new Object())));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).hasSize(2);
        assertThat(updates.get(0).title()).isEqualTo("Issue: Issue 1");
        assertThat(updates.get(1).title()).isEqualTo("PR: PR 1");
    }
}
