package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {
    Link add(long chatId, Link link);

    Link remove(long chatId, URI url);

    List<Link> findAllByChatId(long chatId);

    List<Link> findAll();

    List<Long> findChatIdsByUrl(URI url);

    boolean existsByUrl(long chatId, URI url);

    void updateLastUpdated(URI url, OffsetDateTime time);

    List<Link> findOldest(int limit);
}
