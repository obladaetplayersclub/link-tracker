package backend.academy.linktracker.scrapper.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(Long chatId) {
        super("Чат с id " + chatId + " уже существует");
    }
}
