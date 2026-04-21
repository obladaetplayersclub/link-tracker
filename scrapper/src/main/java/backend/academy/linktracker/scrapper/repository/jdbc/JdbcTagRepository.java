package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.TagRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "SQL")
public class JdbcTagRepository implements TagRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void add(String name) {
        jdbcTemplate.update("INSERT INTO tags (name) VALUES (?) ON CONFLICT DO NOTHING", name);
    }

    @Override
    public void remove(String name) {
        jdbcTemplate.update("DELETE FROM tags WHERE name = ?", name);
    }

    @Override
    public List<String> findAll() {
        return jdbcTemplate.queryForList("SELECT name FROM tags", String.class);
    }

    @Override
    public boolean exists(String name) {
        return jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM tags WHERE name = ?)", Boolean.class, name);
    }

    @Override
    @Transactional
    public void addTagToLink(long linkId, String tagName) {
        jdbcTemplate.update("INSERT INTO tags (name) VALUES (?) ON CONFLICT DO NOTHING", tagName);
        var tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, tagName);
        jdbcTemplate.update(
                "INSERT INTO link_tags (link_id, tag_id) VALUES (?,?) ON CONFLICT DO NOTHING", linkId, tagId);
    }

    @Override
    public void removeTagFromLink(long linkId, String tagName) {
        var tagId = jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, tagName);
        jdbcTemplate.update("DELETE FROM link_tags WHERE link_id = ? AND tag_id = ?", linkId, tagId);
    }

    @Override
    public List<String> findTagsByLinkId(long linkId) {
        return jdbcTemplate.queryForList(
                "SELECT t.name FROM tags t JOIN link_tags lt ON t.id = lt.tag_id WHERE lt.link_id = ?",
                String.class,
                linkId);
    }
}
