package backend.academy.linktracker.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractRepositoryTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected ChatRepository chatRepository;

    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM link_tags");
        jdbcTemplate.update("DELETE FROM chat_links");
        jdbcTemplate.update("DELETE FROM links");
        jdbcTemplate.update("DELETE FROM tags");
        jdbcTemplate.update("DELETE FROM chats");
    }

    @Test
    void registerChat_shouldSaveChat() {
        chatRepository.register(123L);
        assertThat(chatRepository.exists(123L)).isTrue();
    }

    @Test
    void deleteChat_shouldRemoveChat() {
        chatRepository.register(123L);
        chatRepository.delete(123L);
        assertThat(chatRepository.exists(123L)).isFalse();
    }

    @Test
    void existsChat_shouldReturnFalseForNonExistent() {
        assertThat(chatRepository.exists(999L)).isFalse();
    }

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

    @Test
    void addTag_shouldSaveTag() {
        tagRepository.add("java");
        assertThat(tagRepository.exists("java")).isTrue();
    }

    @Test
    void removeTag_shouldDeleteTag() {
        tagRepository.add("java");
        tagRepository.remove("java");
        assertThat(tagRepository.exists("java")).isFalse();
    }

    @Test
    void findAllTags_shouldReturnAllTags() {
        tagRepository.add("java");
        tagRepository.add("spring");

        List<String> tags = tagRepository.findAll();

        assertThat(tags).containsExactlyInAnyOrder("java", "spring");
    }

    @Test
    void addTagToLink_shouldLinkTagToLink() {
        chatRepository.register(1L);
        Link link = linkRepository.add(1L, new Link(null, URI.create("https://github.com/a/b"), List.of(), null));

        tagRepository.addTagToLink(link.getId(), "java");

        List<String> tags = tagRepository.findTagsByLinkId(link.getId());
        assertThat(tags).containsExactly("java");
    }

    @Test
    void removeTagFromLink_shouldUnlinkTag() {
        chatRepository.register(1L);
        Link link = linkRepository.add(1L, new Link(null, URI.create("https://github.com/a/b"), List.of(), null));
        tagRepository.addTagToLink(link.getId(), "java");

        tagRepository.removeTagFromLink(link.getId(), "java");

        List<String> tags = tagRepository.findTagsByLinkId(link.getId());
        assertThat(tags).isEmpty();
    }
}
