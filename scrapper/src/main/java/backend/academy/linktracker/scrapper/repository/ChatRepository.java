package backend.academy.linktracker.scrapper.repository;

public interface ChatRepository {
    void register(long chatId);

    void delete(long chatId);

    boolean exists(long chatId);
}
