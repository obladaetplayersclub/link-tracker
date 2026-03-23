package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdater {
    private final LinkRepository linkRepository;
    private final List<LinkParser> parsers;
    private final BotClient botClient;

    public void update() {
        List<Link> links = linkRepository.findAll();
        for (Link link : links) {
            try {
                checkLink(link);
            } catch (Exception e) {
                log.atWarn().addKeyValue("link_url", link.getUrl()).setCause(e).log("Ошибка при проверке ссылки");
            }
        }
    }

    private void checkLink(Link link) {
        LinkParser parser = parsers.stream()
                .filter(p -> p.supports(link.getUrl()))
                .findFirst()
                .orElse(null);
        if (parser == null) return;
        OffsetDateTime lastUpdated = parser.checkUpdate(link.getUrl());
        if (lastUpdated == null) return;
        if (link.getLastUpdated() == null || lastUpdated.isAfter(link.getLastUpdated())) {
            List<Long> chatIds = linkRepository.findChatIdsByUrl(link.getUrl());
            log.atInfo()
                    .addKeyValue("link_url", link.getUrl())
                    .addKeyValue("has_update", true)
                    .addKeyValue("subscribers_count", chatIds.size())
                    .log("Обнаружено обновление ссылки");
            botClient.sendUpdate(
                    new LinkUpdate(link.getId(), link.getUrl(), "Обновление по ссылке: " + link.getUrl(), chatIds));
            linkRepository.updateLastUpdated(link.getUrl(), lastUpdated);
        }
    }
}
