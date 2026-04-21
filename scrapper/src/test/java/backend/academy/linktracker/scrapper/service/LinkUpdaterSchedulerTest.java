package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.dto.GitHub.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.client.dto.GitHub.GitHubUser;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.repository.ComponentTest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = "app.database-access-type=SQL")
public class LinkUpdaterSchedulerTest extends ComponentTest {
    @Autowired
    LinkUpdater linkUpdater;

    @MockitoBean
    GitHubClient gitHubClient;

    @MockitoBean
    MessageSender messageSender;

    @Test
    void happyPath_shouldDetectUpdate_sendMessage_andSaveChanges() {
        OffsetDateTime oldTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        String url = "https://github.com/owner/repo";

        jdbcTemplate.update("INSERT INTO chats (chat_id) VALUES (?)", 1L);
        jdbcTemplate.update("INSERT INTO links (url, last_updated) VALUES (?, ?)", url, oldTime);
        jdbcTemplate.update(
                "INSERT INTO chat_links (chat_id, link_id) VALUES (?, (SELECT id FROM links WHERE url = ?))", 1L, url);

        GitHubRepoResponse.GitHubIssueResponse issue = new GitHubRepoResponse.GitHubIssueResponse(
                "Fix bug",
                new GitHubUser("kostya"),
                OffsetDateTime.of(2025, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                "Fixed a critical bug",
                null);
        when(gitHubClient.getIssues(eq("owner"), eq("repo"), anyString())).thenReturn(java.util.List.of(issue));

        linkUpdater.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).send(captor.capture());
        LinkUpdate sent = captor.getValue();
        assertThat(sent.tgChatIds()).containsExactly(1L);
        assertThat(sent.description()).contains("Fix bug");
        assertThat(sent.description()).contains("kostya");

        OffsetDateTime updatedTime =
                jdbcTemplate.queryForObject("SELECT last_updated FROM links WHERE url = ?", OffsetDateTime.class, url);
        assertThat(updatedTime).isAfter(oldTime);
    }
}
