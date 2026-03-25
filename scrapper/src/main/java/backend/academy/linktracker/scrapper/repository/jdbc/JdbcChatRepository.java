package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.database-access-type", havingValue = "SQL")
public class JdbcChatRepository implements ChatRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void register(long chatId) {
        jdbcTemplate.update("INSERT INTO chats (chat_id) VALUES (?)", chatId);
    }

    @Override
    public void delete(long chatId) {
        jdbcTemplate.update("DELETE FROM chats WHERE chat_id = ?", chatId);
    }

    @Override
    public boolean exists(long chatId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM chats WHERE chat_id = ?)", Boolean.class, chatId);
    }
}
