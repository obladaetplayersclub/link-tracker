package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.parser.LinkUpdateInfo;
import backend.academy.linktracker.scrapper.properties.AppProperties;
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
class LinkUpdaterTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkParser gitHubParser;

    @Mock
    private LinkParser stackOverflowParser;

    @Mock
    private MessageSender messageSender;

    private LinkUpdater linkUpdater;

    private static final URI GITHUB_URL = URI.create("https://github.com/user/repo");
    private static final URI SO_URL = URI.create("https://stackoverflow.com/questions/12345/title");
    private static final OffsetDateTime OLD_TIME = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime NEW_TIME = OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.setBatchSize(100);
        appProperties.setThreadCount(2);
        linkUpdater = new LinkUpdater(
                linkRepository, List.of(gitHubParser, stackOverflowParser), messageSender, appProperties);
    }

    @Test
    void update_shouldSendNotification_whenGitHubIssueFound() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any()))
                .thenReturn(List.of(new LinkUpdateInfo("Issue: Bug fix", "kostya", NEW_TIME, "Fixed the bug")));
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L, 200L));

        linkUpdater.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).send(captor.capture());
        LinkUpdate sent = captor.getValue();
        assertThat(sent.tgChatIds()).containsExactly(100L, 200L);
        assertThat(sent.url()).isEqualTo(GITHUB_URL);
        assertThat(sent.description()).contains("Issue: Bug fix");
        assertThat(sent.description()).contains("kostya");
    }

    @Test
    void update_shouldSendNotification_whenStackOverflowAnswerFound() {
        Link link = new Link(2L, SO_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));
        when(stackOverflowParser.supports(SO_URL)).thenReturn(true);
        when(stackOverflowParser.checkUpdates(eq(SO_URL), any()))
                .thenReturn(List.of(new LinkUpdateInfo("Новый ответ", "john", NEW_TIME, "Try using Stream API")));
        when(linkRepository.findChatIdsByUrl(SO_URL)).thenReturn(List.of(300L));

        linkUpdater.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).send(captor.capture());
        LinkUpdate sent = captor.getValue();
        assertThat(sent.description()).contains("Новый ответ");
        assertThat(sent.description()).contains("john");
        assertThat(sent.description()).contains("Try using Stream API");
    }

    @Test
    void update_shouldNotSend_whenNoUpdatesFound() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any())).thenReturn(List.of());

        linkUpdater.update();

        verify(messageSender, never()).send(any());
    }

    @Test
    void update_shouldNotCrash_whenApiThrowsException() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any())).thenThrow(new RestClientException("API error"));

        linkUpdater.update();

        verify(messageSender, never()).send(any());
    }

    @Test
    void update_shouldSkipLink_whenNoParserSupports() {
        URI unsupportedUrl = URI.create("https://example.com/something");
        Link link = new Link(1L, unsupportedUrl, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));
        when(gitHubParser.supports(unsupportedUrl)).thenReturn(false);
        when(stackOverflowParser.supports(unsupportedUrl)).thenReturn(false);

        linkUpdater.update();

        verify(messageSender, never()).send(any());
    }

    @Test
    void update_shouldProcessBatch_andIsolateErrors() {
        Link goodLink = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        Link badLink = new Link(2L, SO_URL, List.of(), OLD_TIME);

        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(goodLink, badLink));

        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any()))
                .thenReturn(List.of(new LinkUpdateInfo("PR: New feature", "dev", NEW_TIME, "Added feature")));
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L));

        when(stackOverflowParser.supports(SO_URL)).thenReturn(true);
        when(stackOverflowParser.checkUpdates(eq(SO_URL), any())).thenThrow(new RestClientException("SO down"));

        linkUpdater.update();

        verify(messageSender, times(1)).send(any());
        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(messageSender).send(captor.capture());
        assertThat(captor.getValue().url()).isEqualTo(GITHUB_URL);
    }

    @Test
    void update_shouldSendMultipleNotifications_whenMultipleUpdatesForOneLink() {
        Link link = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdates(eq(GITHUB_URL), any()))
                .thenReturn(List.of(
                        new LinkUpdateInfo("Issue: Bug 1", "user1", NEW_TIME, "First bug"),
                        new LinkUpdateInfo("PR: Fix 2", "user2", NEW_TIME, "Second fix")));
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L));

        linkUpdater.update();

        verify(messageSender, times(2)).send(any());
    }

    @Test
    void update_shouldDoNothing_whenNoLinksInBatch() {
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of());

        linkUpdater.update();

        verify(messageSender, never()).send(any());
    }
}
