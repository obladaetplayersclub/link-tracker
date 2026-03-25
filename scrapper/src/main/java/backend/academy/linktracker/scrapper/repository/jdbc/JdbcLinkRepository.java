package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.database-access-type", havingValue = "SQL")
public class JdbcLinkRepository implements LinkRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean existsByUrl(long chatId, URI url) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM links l JOIN chat_links cl ON l.id = cl.link_id JOIN chats c ON cl.chat_id = c.id WHERE l.url = ? AND c.chat_id = ?)",
                Boolean.class,
                url.toString(),
                chatId);
    }

    @Override
    @Transactional
    public Link add(long chatId, Link link) {
        Long linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM links WHERE url = ?", Long.class, link.getUrl().toString());

        if (linkId == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    con -> {
                        PreparedStatement ps = con.prepareStatement(
                                "INSERT INTO links (url, last_updated) VALUES (?, NOW())", new String[] {"id"});
                        ps.setString(1, link.getUrl().toString());
                        return ps;
                    },
                    keyHolder);
            linkId = keyHolder.getKey().longValue();
        }

        Long chatPk = jdbcTemplate.queryForObject("SELECT id FROM chats WHERE chat_id = ?", Long.class, chatId);

        jdbcTemplate.update(
                "INSERT INTO chat_links (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING", chatPk, linkId);

        if (link.getTags() != null) {
            for (String tagName : link.getTags()) {
                jdbcTemplate.update("INSERT INTO tags (name) VALUES (?) ON CONFLICT DO NOTHING", tagName);
                Long tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, tagName);
                jdbcTemplate.update(
                        "INSERT INTO link_tags (link_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING", linkId, tagId);
            }
        }
        OffsetDateTime lastUpdated = jdbcTemplate.queryForObject(
                "SELECT last_updated FROM links WHERE id = ?", OffsetDateTime.class, linkId);

        return new Link(linkId, link.getUrl(), link.getTags(), lastUpdated);
    }

    @Override
    @Transactional
    public Link remove(long chatId, URI url) {
        Long chatPk = jdbcTemplate.queryForObject("SELECT id FROM chats WHERE chat_id = ?", Long.class, chatId);
        Long linkId = jdbcTemplate.queryForObject("SELECT id FROM links WHERE url = ?", Long.class, url.toString());

        List<String> tags = jdbcTemplate.queryForList(
                "SELECT t.name FROM tags t JOIN link_tags lt ON t.id = lt.tag_id WHERE lt.link_id = ?",
                String.class,
                linkId);
        OffsetDateTime lastUpdated = jdbcTemplate.queryForObject(
                "SELECT last_updated FROM links WHERE id = ?", OffsetDateTime.class, linkId);

        Link result = new Link(linkId, url, tags, lastUpdated);

        jdbcTemplate.update("DELETE FROM chat_links WHERE chat_id = ? AND link_id = ?", chatPk, linkId);

        Integer remaining =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_links WHERE link_id = ?", Integer.class, linkId);
        if (remaining != null && remaining == 0) {
            jdbcTemplate.update("DELETE FROM link_tags WHERE link_id = ?", linkId);
            jdbcTemplate.update("DELETE FROM links WHERE id = ?", linkId);
        }

        return result;
    }

    @Override
    public List<Link> findAllByChatId(long chatId) {
        return jdbcTemplate.query(
                "SELECT l.id, l.url, l.last_updated FROM links l "
                        + "JOIN chat_links cl ON l.id = cl.link_id "
                        + "JOIN chats c ON cl.chat_id = c.id "
                        + "WHERE c.chat_id = ?",
                (rs, rowNum) -> {
                    long id = rs.getLong("id");
                    URI linkUrl = URI.create(rs.getString("url"));
                    OffsetDateTime lastUpd = rs.getObject("last_updated", OffsetDateTime.class);
                    List<String> tags = findTagsByLinkId(id);
                    return new Link(id, linkUrl, tags, lastUpd);
                },
                chatId);
    }

    @Override
    public List<Link> findAll() {
        return jdbcTemplate.query("SELECT id, url, last_updated FROM links", (rs, rowNum) -> {
            long id = rs.getLong("id");
            URI linkUrl = URI.create(rs.getString("url"));
            OffsetDateTime lastUpd = rs.getObject("last_updated", OffsetDateTime.class);
            List<String> tags = findTagsByLinkId(id);
            return new Link(id, linkUrl, tags, lastUpd);
        });
    }

    @Override
    public List<Long> findChatIdsByUrl(URI url) {
        return jdbcTemplate.queryForList(
                "SELECT c.chat_id FROM chats c "
                        + "JOIN chat_links cl ON c.id = cl.chat_id "
                        + "JOIN links l ON cl.link_id = l.id "
                        + "WHERE l.url = ?",
                Long.class,
                url.toString());
    }

    @Override
    public void updateLastUpdated(URI url, OffsetDateTime time) {
        jdbcTemplate.update("UPDATE links SET last_updated = ? WHERE url = ?", time, url.toString());
    }

    private List<String> findTagsByLinkId(long linkId) {
        return jdbcTemplate.queryForList(
                "SELECT t.name FROM tags t JOIN link_tags lt ON t.id = lt.tag_id WHERE lt.link_id = ?",
                String.class,
                linkId);
    }
}
