package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@ConditionalOnProperty(name = "app.database-access-type", havingValue = "ORM")
public interface JpaChatRepository extends JpaRepository<ChatEntity, Long>, ChatRepository {

    Optional<ChatEntity> findByChatId(long chatId);

    @Override
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatEntity c WHERE c.chatId = :chatId")
    void delete(@Param("chatId") long chatId);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChatEntity c WHERE c.chatId = :chatId")
    boolean exists(@Param("chatId") long chatId);

    @Override
    default void register(long chatId) {
        ChatEntity entity = new ChatEntity();
        entity.setChatId(chatId);
        save(entity);
    }
}
