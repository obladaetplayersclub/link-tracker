package backend.academy.linktracker.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.properties.LinkUpdaterProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkChecker linkChecker;

    @Mock
    private ThreadPoolTaskExecutor linkUpdaterExecutor;

    private LinkUpdater linkUpdater;

    private static final URI GITHUB_URL = URI.create("https://github.com/user/repo");
    private static final URI SO_URL = URI.create("https://stackoverflow.com/questions/12345/title");
    private static final OffsetDateTime OLD_TIME = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        LinkUpdaterProperties props = new LinkUpdaterProperties();
        props.setBatchSize(100);
        linkUpdater = new LinkUpdater(linkRepository, props, linkUpdaterExecutor, linkChecker);

        org.mockito.Mockito.lenient()
                .when(linkUpdaterExecutor.submit(any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(0);
                    task.run();
                    return CompletableFuture.completedFuture(null);
                });
    }

    @Test
    void update_shouldCallCheckLink_forEachLinkInBatch() {
        Link link1 = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        Link link2 = new Link(2L, SO_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link1, link2));

        linkUpdater.update();

        verify(linkChecker).checkLink(link1);
        verify(linkChecker).checkLink(link2);
    }

    @Test
    void update_shouldDoNothing_whenNoLinksInBatch() {
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of());

        linkUpdater.update();

        verify(linkChecker, never()).checkLink(any());
    }

    @Test
    void update_shouldIsolateErrors_whenOneCheckLinkFails() {
        Link goodLink = new Link(1L, GITHUB_URL, List.of(), OLD_TIME);
        Link badLink = new Link(2L, SO_URL, List.of(), OLD_TIME);
        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(badLink, goodLink));
        org.mockito.Mockito.doThrow(new RuntimeException("API error"))
                .when(linkChecker)
                .checkLink(badLink);

        linkUpdater.update();

        verify(linkChecker).checkLink(goodLink);
    }
}
