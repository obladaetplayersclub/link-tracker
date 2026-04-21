package backend.academy.linktracker.scrapper.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ComponentTest extends ContainersConfiguration {
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
}
