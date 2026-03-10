package backend.academy.linktracker.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.dto.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.client.dto.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.ChainLinkParser;
import backend.academy.linktracker.scrapper.parser.GitHubParsedLink;
import backend.academy.linktracker.scrapper.parser.StackOverflowParsedLink;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
    private ChainLinkParser linkParser;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private BotClient botClient;

    private StackoverflowProperties stackoverflowProperties;

    private LinkUpdater linkUpdater;

    private static final URI GITHUB_URL = URI.create("https://github.com/user/repo");
    private static final URI SO_URL = URI.create("https://stackoverflow.com/questions/12345/title");

    @BeforeEach
    void setUp() {
        stackoverflowProperties = new StackoverflowProperties();
        stackoverflowProperties.setKey("test-key");
        stackoverflowProperties.setAccessToken("test-token");
        linkUpdater = new LinkUpdater(
                linkRepository, linkParser, gitHubClient, stackOverflowClient, stackoverflowProperties, botClient);
    }

    @Test
    void update_ShouldNotifyOnlySubscribers_WhenGitHubLinkUpdated() {
        OffsetDateTime oldTime = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime newTime = OffsetDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Link link = new Link(1L, GITHUB_URL, List.of(), oldTime);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkParser.parse(GITHUB_URL)).thenReturn(Optional.of(new GitHubParsedLink("user", "repo")));
        when(gitHubClient.getRepository("user", "repo")).thenReturn(new GitHubRepoResponse(newTime, newTime));
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
        when(linkParser.parse(GITHUB_URL)).thenReturn(Optional.of(new GitHubParsedLink("user", "repo")));
        when(gitHubClient.getRepository("user", "repo")).thenReturn(new GitHubRepoResponse(sameTime, sameTime));

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotCrash_WhenGitHubClientThrowsException() {
        Link link = new Link(1L, GITHUB_URL, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkParser.parse(GITHUB_URL)).thenReturn(Optional.of(new GitHubParsedLink("user", "repo")));
        when(gitHubClient.getRepository("user", "repo")).thenThrow(new RestClientException("API error"));

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotCrash_WhenStackOverflowClientThrowsException() {
        Link link = new Link(1L, SO_URL, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkParser.parse(SO_URL)).thenReturn(Optional.of(new StackOverflowParsedLink(12345L)));
        when(stackOverflowClient.getQuestion(anyLong(), anyString(), anyString(), anyString()))
                .thenThrow(new RestClientException("SO API error"));

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldNotNotify_WhenStackOverflowReturnsEmptyItems() {
        Link link = new Link(1L, SO_URL, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkParser.parse(SO_URL)).thenReturn(Optional.of(new StackOverflowParsedLink(12345L)));
        when(stackOverflowClient.getQuestion(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(new StackOverflowQuestionResponse(List.of()));

        linkUpdater.update();

        verify(botClient, never()).sendUpdate(any());
    }

    @Test
    void update_ShouldSkipLink_WhenParserReturnsEmpty() {
        URI unsupportedUrl = URI.create("https://example.com/something");
        Link link = new Link(1L, unsupportedUrl, List.of(), null);
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkParser.parse(unsupportedUrl)).thenReturn(Optional.empty());

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

        when(linkParser.parse(GITHUB_URL)).thenReturn(Optional.of(new GitHubParsedLink("user", "repo")));
        when(gitHubClient.getRepository("user", "repo")).thenReturn(new GitHubRepoResponse(newTime, newTime));
        when(linkRepository.findChatIdsByUrl(GITHUB_URL)).thenReturn(List.of(100L));

        when(linkParser.parse(otherUrl)).thenReturn(Optional.of(new GitHubParsedLink("other", "repo")));
        when(gitHubClient.getRepository("other", "repo")).thenReturn(new GitHubRepoResponse(newTime, newTime));

        linkUpdater.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(botClient).sendUpdate(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().url()).isEqualTo(GITHUB_URL);
    }
}
