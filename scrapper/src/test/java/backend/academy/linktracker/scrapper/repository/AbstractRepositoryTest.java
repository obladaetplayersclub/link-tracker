package backend.academy.linktracker.scrapper.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public class AbstractRepositoryTest {
    @Autowired
    protected ChatRepository chatRepository;

    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected TagRepository tagRepository;

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

}
