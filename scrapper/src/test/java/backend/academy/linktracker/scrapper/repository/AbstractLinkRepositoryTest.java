package backend.academy.linktracker.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class AbstractLinkRepositoryTest extends ComponentTest {

    @Test
    void addLink_shouldSaveLink() {
        chatRepository.register(1L);
        Link link = new Link(null, URI.create("https://github.com/owner/repo"), List.of(), null);

        Link saved = linkRepository.add(1L, link);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo(URI.create("https://github.com/owner/repo"));
    }

    @Test
    void addLink_withTags_shouldSaveTags() {
        chatRepository.register(1L);
        Link link = new Link(null, URI.create("https://github.com/owner/repo"), List.of("java", "spring"), null);

        Link saved = linkRepository.add(1L, link);

        assertThat(saved.getTags()).containsExactlyInAnyOrder("java", "spring");
    }

    @Test
    void removeLink_shouldDeleteLink() {
        chatRepository.register(1L);
        URI url = URI.create("https://github.com/owner/repo");
        linkRepository.add(1L, new Link(null, url, List.of(), null));

        linkRepository.remove(1L, url);

        assertThat(linkRepository.existsByUrl(1L, url)).isFalse();
    }

    @Test
    void findAllByChatId_shouldReturnLinksForChat() {
        chatRepository.register(1L);
        linkRepository.add(1L, new Link(null, URI.create("https://github.com/a/b"), List.of(), null));
        linkRepository.add(1L, new Link(null, URI.create("https://github.com/c/d"), List.of(), null));

        List<Link> links = linkRepository.findAllByChatId(1L);

        assertThat(links).hasSize(2);
    }

    @Test
    void findAll_shouldReturnAllLinks() {
        chatRepository.register(1L);
        chatRepository.register(2L);
        linkRepository.add(1L, new Link(null, URI.create("https://github.com/a/b"), List.of(), null));
        linkRepository.add(2L, new Link(null, URI.create("https://github.com/c/d"), List.of(), null));

        List<Link> links = linkRepository.findAll();

        assertThat(links).hasSize(2);
    }

    @Test
    void findChatIdsByUrl_shouldReturnSubscribedChats() {
        chatRepository.register(1L);
        chatRepository.register(2L);
        URI url = URI.create("https://github.com/a/b");
        linkRepository.add(1L, new Link(null, url, List.of(), null));
        linkRepository.add(2L, new Link(null, url, List.of(), null));

        List<Long> chatIds = linkRepository.findChatIdsByUrl(url);

        assertThat(chatIds).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void existsByUrl_shouldReturnTrueForExistingLink() {
        chatRepository.register(1L);
        URI url = URI.create("https://github.com/a/b");
        linkRepository.add(1L, new Link(null, url, List.of(), null));

        assertThat(linkRepository.existsByUrl(1L, url)).isTrue();
    }

    @Test
    void existsByUrl_shouldReturnFalseForOtherChat() {
        chatRepository.register(1L);
        chatRepository.register(2L);
        URI url = URI.create("https://github.com/a/b");
        linkRepository.add(1L, new Link(null, url, List.of(), null));

        assertThat(linkRepository.existsByUrl(2L, url)).isFalse();
    }
}
