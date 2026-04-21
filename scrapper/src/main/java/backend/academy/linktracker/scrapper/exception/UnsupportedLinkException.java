package backend.academy.linktracker.scrapper.exception;

import java.net.URI;

public class UnsupportedLinkException extends RuntimeException {
    public UnsupportedLinkException(URI url) {
        super("Данная ссылка: " + url + " не может быть обработана нашими парсерами");
    }
}
