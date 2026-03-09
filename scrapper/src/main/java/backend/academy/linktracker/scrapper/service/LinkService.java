package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;

    public Link add(long chatId, URI url, List<String> tags) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        if (linkRepository.existsByUrl(chatId, url)) {
            throw new LinkAlreadyTrackedException(url);
        }
        Link link = new Link(null, url, tags, OffsetDateTime.now());
        return linkRepository.add(chatId, link);
    }

    public Link remove(long chatId, URI url) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        if (!linkRepository.existsByUrl(chatId, url)) {
            throw new LinkNotFoundException(chatId, url);
        }
        return linkRepository.remove(chatId, url);
    }

    public List<Link> findAllByChatId(long chatId) {
        if (!chatRepository.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        return linkRepository.findAllByChatId(chatId);
    }
}
