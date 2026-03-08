package backend.academy.linktracker.scrapper.repository;

public interface ChatRepository {
    void register(Long chatId);

    void delete(Long chatId);

    boolean exists(Long chatId);
}
