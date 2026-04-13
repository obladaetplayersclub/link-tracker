package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.parser.LinkUpdateInfo;
import backend.academy.linktracker.scrapper.properties.AppProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdater {
    private final LinkRepository linkRepository;
    private final List<LinkParser> parsers;
    private final MessageSender messageSender;
    private final AppProperties appProperties;

    public void update() {
        List<Link> links = linkRepository.findOldest(appProperties.getBatchSize());
        if (links.isEmpty()) return;

        int threadCount = appProperties.getThreadCount();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<List<Link>> chunks = partition(links, threadCount);

        List<Future<?>> futures = new ArrayList<>();
        for (List<Link> chunk : chunks) {
            futures.add(executor.submit(() -> processChunk(chunk)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.atError().setCause(e).log("Ошибка в потоке обработки ссылок");
            }
        }

        executor.shutdown();
    }

    private void processChunk(List<Link> chunk) {
        for (Link link : chunk) {
            try {
                checkLink(link);
            } catch (Exception e) {
                log.atWarn().addKeyValue("link_url", link.getUrl()).setCause(e).log("Ошибка при проверке ссылки");
            }
        }
    }

    @Transactional
    protected void checkLink(Link link) {
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
                    "%s\nАвтор: %s\nВремя: %s\n%s", info.title(), info.author(), info.createdAt(), info.preview());
            log.atInfo()
                    .addKeyValue("link_url", link.getUrl())
                    .addKeyValue("update_title", info.title())
                    .addKeyValue("subscribers_count", chatIds.size())
                    .log("Обнаружено обновление ссылки");
            messageSender.send(new LinkUpdate(link.getId(), link.getUrl(), description, chatIds));
        }
        linkRepository.updateLastUpdated(link.getUrl(), OffsetDateTime.now());
    }

    private <T> List<List<T>> partition(List<T> list, int n) {
        int size = (int) Math.ceil((double) list.size() / n);
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }
}
