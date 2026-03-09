package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<Long, List<Link>> linksByChat = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Link add(long chatId, Link link) {
        link.setId(idGenerator.getAndIncrement());
        linksByChat.computeIfAbsent(chatId, k -> new CopyOnWriteArrayList<>()).add(link);
        return link;
    }

    @Override
    public Link remove(long chatId, URI url) {
        List<Link> links = linksByChat.get(chatId);
        if (links == null) {
            return null;
        }
        Link found =
                links.stream().filter(l -> l.getUrl().equals(url)).findFirst().orElse(null);
        if (found != null) {
            links.remove(found);
        }
        return found;
    }

    @Override
    public List<Link> findAllByChatId(long chatId) {
        return linksByChat.getOrDefault(chatId, List.of());
    }

    @Override
    public List<Link> findAll() {
        return linksByChat.values().stream().flatMap(List::stream).distinct().toList();
    }

    @Override
    public List<Long> findChatIdsByUrl(URI url) {
        return linksByChat.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(l -> l.getUrl().equals(url)))
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public boolean existsByUrl(long chatId, URI url) {
        return linksByChat.getOrDefault(chatId, List.of()).stream()
                .anyMatch(l -> l.getUrl().equals(url));
    }

    @Override
    public void updateLastUpdated(URI url, OffsetDateTime time) {
        linksByChat.values().stream()
                .flatMap(List::stream)
                .filter(l -> l.getUrl().equals(url))
                .forEach(l -> l.setLastUpdated(time));
    }
}
