package backend.academy.linktracker.scrapper.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.database-access-type=SQL")
public class JdbcChatRepositoryTest extends AbstractChatRepositoryTest {}
