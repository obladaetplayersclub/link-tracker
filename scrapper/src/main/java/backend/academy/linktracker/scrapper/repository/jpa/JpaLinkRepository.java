package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "ORM")
public class JpaLinkRepository implements LinkRepository {

    private final JpaLinkEntityRepository linkEntityRepository;
    private final JpaChatRepository chatRepository;
    private final JpaTagEntityRepository tagEntityRepository;

    @Override
    @Transactional
    public Link add(long chatId, Link link) {
        ChatEntity chat = chatRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new NoSuchElementException("Chat not found: " + chatId));

        LinkEntity linkEntity = linkEntityRepository
                .findByUrl(link.getUrl().toString())
                .orElseGet(() -> {
                    LinkEntity newLink = new LinkEntity();
                    newLink.setUrl(link.getUrl().toString());
                    newLink.setLastUpdated(OffsetDateTime.now());
                    return linkEntityRepository.save(newLink);
                });

        linkEntity.getChats().add(chat);

        if (link.getTags() != null) {
            for (String tagName : link.getTags()) {
                TagEntity tag = tagEntityRepository.findByName(tagName).orElseGet(() -> {
                    TagEntity newTag = new TagEntity();
                    newTag.setName(tagName);
                    return tagEntityRepository.save(newTag);
                });
                linkEntity.getTags().add(tag);
            }
        }

        linkEntityRepository.save(linkEntity);
        return toLink(linkEntity);
    }

    @Override
    @Transactional
    public Link remove(long chatId, URI url) {
        ChatEntity chat = chatRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new NoSuchElementException("Chat not found: " + chatId));

        LinkEntity linkEntity = linkEntityRepository
                .findByUrl(url.toString())
                .orElseThrow(() -> new NoSuchElementException("Link not found: " + url));

        linkEntity.getChats().remove(chat);

        Link result = toLink(linkEntity);

        if (linkEntity.getChats().isEmpty()) {
            linkEntityRepository.delete(linkEntity);
        } else {
            linkEntityRepository.save(linkEntity);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> findAllByChatId(long chatId) {
        return linkEntityRepository.findAll().stream()
                .filter(link ->
                        link.getChats().stream().anyMatch(c -> c.getChatId().equals(chatId)))
                .map(this::toLink)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> findAll() {
        return linkEntityRepository.findAll().stream().map(this::toLink).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findChatIdsByUrl(URI url) {
        return linkEntityRepository
                .findByUrl(url.toString())
                .map(link -> link.getChats().stream().map(ChatEntity::getChatId).toList())
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUrl(long chatId, URI url) {
        return linkEntityRepository
                .findByUrl(url.toString())
                .map(link ->
                        link.getChats().stream().anyMatch(c -> c.getChatId().equals(chatId)))
                .orElse(false);
    }

    @Override
    @Transactional
    public void updateLastUpdated(URI url, OffsetDateTime time) {
        linkEntityRepository.findByUrl(url.toString()).ifPresent(link -> {
            link.setLastUpdated(time);
            linkEntityRepository.save(link);
        });
    }

    private Link toLink(LinkEntity entity) {
        List<String> tags = entity.getTags().stream().map(TagEntity::getName).collect(Collectors.toList());
        return new Link(entity.getId(), URI.create(entity.getUrl()), tags, entity.getLastUpdated());
    }
}
