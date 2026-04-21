package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "ORM")
public class JpaTagRepository implements TagRepository {

    private final JpaTagEntityRepository tagEntityRepository;
    private final JpaLinkEntityRepository linkEntityRepository;

    @Override
    @Transactional
    public void add(String name) {
        if (!tagEntityRepository.existsByName(name)) {
            TagEntity tag = new TagEntity();
            tag.setName(name);
            tagEntityRepository.save(tag);
        }
    }

    @Override
    @Transactional
    public void remove(String name) {
        tagEntityRepository.findByName(name).ifPresent(tagEntityRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAll() {
        return tagEntityRepository.findAll().stream().map(TagEntity::getName).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String name) {
        return tagEntityRepository.existsByName(name);
    }

    @Override
    @Transactional
    public void addTagToLink(long linkId, String tagName) {
        LinkEntity link = linkEntityRepository
                .findById(linkId)
                .orElseThrow(() -> new NoSuchElementException("Link not found: " + linkId));

        TagEntity tag = tagEntityRepository.findByName(tagName).orElseGet(() -> {
            TagEntity newTag = new TagEntity();
            newTag.setName(tagName);
            return tagEntityRepository.save(newTag);
        });

        link.getTags().add(tag);
        linkEntityRepository.save(link);
    }

    @Override
    @Transactional
    public void removeTagFromLink(long linkId, String tagName) {
        LinkEntity link = linkEntityRepository
                .findById(linkId)
                .orElseThrow(() -> new NoSuchElementException("Link not found: " + linkId));

        tagEntityRepository.findByName(tagName).ifPresent(tag -> {
            link.getTags().remove(tag);
            linkEntityRepository.save(link);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findTagsByLinkId(long linkId) {
        return linkEntityRepository
                .findById(linkId)
                .map(link -> link.getTags().stream().map(TagEntity::getName).toList())
                .orElse(List.of());
    }
}
