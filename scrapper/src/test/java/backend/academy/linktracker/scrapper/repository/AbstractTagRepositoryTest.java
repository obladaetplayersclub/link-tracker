package backend.academy.linktracker.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class AbstractTagRepositoryTest extends AbstractRepositoryTest {

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
