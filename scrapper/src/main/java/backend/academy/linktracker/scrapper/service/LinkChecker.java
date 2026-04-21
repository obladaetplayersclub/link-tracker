package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.parser.LinkUpdateInfo;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class LinkChecker {
    private final LinkRepository linkRepository;
    private final List<LinkParser> parsers;
    private final MessageSender messageSender;

    @Transactional
    public void checkLink(Link link) {
        LinkParser parser = parsers.stream()
                .filter(p -> p.supports(link.getUrl()))
                .findFirst()
                .orElse(null);
        if (parser == null) return;

        OffsetDateTime since = link.getLastUpdated() != null
                ? link.getLastUpdated()
                : OffsetDateTime.now().minusDays(1);

        List<LinkUpdateInfo> updates = parser.checkUpdates(link.getUrl(), since);
        if (updates.isEmpty()) return;

        List<Long> chatIds = linkRepository.findChatIdsByUrl(link.getUrl());
        for (LinkUpdateInfo info : updates) {
            String description = String.format(
                    "%s%nАвтор: %s%nВремя: %s%n%s", info.title(), info.author(), info.createdAt(), info.preview());
            log.atInfo()
                    .addKeyValue("link_url", link.getUrl())
                    .addKeyValue("update_title", info.title())
                    .addKeyValue("subscribers_count", chatIds.size())
                    .log("Обнаружено обновление ссылки");
            messageSender.send(new LinkUpdate(link.getId(), link.getUrl(), description, chatIds));
        }
        linkRepository.updateLastUpdated(link.getUrl(), OffsetDateTime.now());
    }
}
