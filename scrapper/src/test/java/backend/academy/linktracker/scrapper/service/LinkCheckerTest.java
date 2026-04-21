package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.parser.LinkUpdateInfo;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class LinkCheckerTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkParser gitHubParser;

    @Mock
    private LinkParser stackOverflowParser;

    @Mock
    private MessageSender messageSender;

    private LinkChecker linkChecker;

    private static final URI GITHUB_URL = URI.create("https://github.com/user/repo");
    private static final URI SO_URL = URI.create("https://stackoverflow.com/questions/12345/title");
    private static final OffsetDateTime OLD_TIME = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime NEW_TIME = OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        linkChecker = new LinkChecker(linkRepository, List.of(gitHubParser, stackOverflowParser), messageSender);
    }

    @Test
    void checkLink_shouldSendNotification_whenGitHubIssueFound() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any()))
                .thenReturn(List.of(new LinkUpdateInfo("Issue: Bug fix", "kostya", NEW_TIME, "Fixed the bug")));
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L, 200L));

        linkChecker.checkLink(link);

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).send(captor.capture());
        LinkUpdate sent = captor.getValue();
        assertThat(sent.tgChatIds()).containsExactly(100L, 200L);
        assertThat(sent.url()).isEqualTo(GITHUB_URL);
        assertThat(sent.description()).contains("Issue: Bug fix");
        assertThat(sent.description()).contains("kostya");
    }

    @Test
    void checkLink_shouldSendNotification_whenStackOverflowAnswerFound() {
        Link link = new Link(2L, SO_URL, List.of(), OLD_TIME);
        when(stackOverflowParser.supports(SO_URL)).thenReturn(true);
        when(stackOverflowParser.checkUpdates(eq(SO_URL), any()))
                .thenReturn(List.of(new LinkUpdateInfo("Новый ответ", "john", NEW_TIME, "Try using Stream API")));
        when(linkRepository.findChatIdsByUrl(SO_URL)).thenReturn(List.of(300L));

        linkChecker.checkLink(link);

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).send(captor.capture());
        LinkUpdate sent = captor.getValue();
        assertThat(sent.description()).contains("Новый ответ");
        assertThat(sent.description()).contains("john");
        assertThat(sent.description()).contains("Try using Stream API");
    }

    @Test
    void checkLink_shouldNotSend_whenNoUpdatesFound() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any())).thenReturn(List.of());

        linkChecker.checkLink(link);

        verify(messageSender, never()).send(any());
    }

    @Test
    void checkLink_shouldThrow_whenApiThrowsException() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any())).thenThrow(new RestClientException("API error"));

        org.junit.jupiter.api.Assertions.assertThrows(RestClientException.class, () -> linkChecker.checkLink(link));

        verify(messageSender, never()).send(any());
    }

    @Test
    void checkLink_shouldSkip_whenNoParserSupports() {
        URI unsupportedUrl = URI.create("https://example.com/something");
        Link link = new Link(1L, unsupportedUrl, List.of(), OLD_TIME);
        when(gitHubParser.supports(unsupportedUrl)).thenReturn(false);
        when(stackOverflowParser.supports(unsupportedUrl)).thenReturn(false);

        linkChecker.checkLink(link);

        verify(messageSender, never()).send(any());
    }

    @Test
    void checkLink_shouldSendMultipleNotifications_whenMultipleUpdates() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any()))
                .thenReturn(List.of(
                        new LinkUpdateInfo("Issue: Bug 1", "user1", NEW_TIME, "First bug"),
                        new LinkUpdateInfo("PR: Fix 2", "user2", NEW_TIME, "Second fix")));
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L));

        linkChecker.checkLink(link);

        verify(messageSender, times(2)).send(any());
    }
}
