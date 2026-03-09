package backend.academy.linktracker.scrapper.exception;

import java.net.URI;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(Long chatId, URI url) {
        super("Ссылка " + url + " в чате с id " + chatId + " не найдена");
    }
}
