package backend.academy.linktracker.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.LinkParser;
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
    private BotClient botClient;

    private LinkUpdater linkUpdater;

    private static final URI GITHUB_URL = URI.create("https://github.com/user/repo");
    private static final URI SO_URL = URI.create("https://stackoverflow.com/questions/12345/title");

    @BeforeEach
    void setUp() {
        linkUpdater = new LinkUpdater(linkRepository, List.of(gitHubParser, stackOverflowParser), botClient);
    }

    @Test
    void update_ShouldNotifyOnlySubscribers_WhenGitHubLinkUpdated() {
        OffsetDateTime oldTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime newTime = OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Link link = new Link(1L, GITHUB_URL, List.of(), oldTime);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdate(GITHUB_URL)).thenReturn(newTime);
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L, 200L));

        linkUpdater.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(botClient).sendUpdate(captor.capture());

        LinkUpdate sentUpdate = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(sentUpdate.tgChatIds()).containsExactly(100L, 200L);
        org.assertj.core.api.Assertions.assertThat(sentUpdate.url()).isEqualTo(GITHUB_URL);
    }

    @Test
    void update_ShouldNotNotify_WhenNoNewUpdates() {
        OffsetDateTime sameTime = OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Link link = new Link(1L, GITHUB_URL, List.of(), sameTime);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdate(GITHUB_URL)).thenReturn(sameTime);

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotCrash_WhenGitHubClientThrowsException() {
        Link link = new Link(1L, GITHUB_URL, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdate(GITHUB_URL)).thenThrow(new RestClientException("API error"));

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotCrash_WhenStackOverflowClientThrowsException() {
        Link link = new Link(1L, SO_URL, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(stackOverflowParser.supports(SO_URL)).thenReturn(true);
        when(stackOverflowParser.checkUpdate(SO_URL)).thenThrow(new RestClientException("SO API error"));

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotNotify_WhenCheckUpdateReturnsNull() {
        Link link = new Link(1L, SO_URL, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(stackOverflowParser.supports(SO_URL)).thenReturn(true);
        when(stackOverflowParser.checkUpdate(SO_URL)).thenReturn(null);

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldSkipLink_WhenNoParserSupports() {
        URI unsupportedUrl = URI.create("https://example.com/something");
        Link link = new Link(1L, unsupportedUrl, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(gitHubParser.supports(unsupportedUrl)).thenReturn(false);
        when(stackOverflowParser.supports(unsupportedUrl)).thenReturn(false);

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotifyOnlyForUpdatedLinks_WhenMultipleLinksExist() {
        OffsetDateTime oldTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime newTime = OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Link updatedLink = new Link(1L, GITHUB_URL, List.of(), oldTime);
        URI otherUrl = URI.create("https://github.com/other/repo");
        Link notUpdatedLink = new Link(2L, otherUrl, List.of(), newTime);

        when(linkRepository.findAll()).thenReturn(List.of(updatedLink, notUpdatedLink));

        when(gitHubParser.supports(GITHUB_URL)).thenReturn(true);
        when(gitHubParser.checkUpdate(GITHUB_URL)).thenReturn(newTime);
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L));

        when(gitHubParser.supports(otherUrl)).thenReturn(true);
        when(gitHubParser.checkUpdate(otherUrl)).thenReturn(newTime);

        linkUpdater.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(botClient).sendUpdate(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().url()).isEqualTo(GITHUB_URL);
    }
}
