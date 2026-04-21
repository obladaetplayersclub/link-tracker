package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.properties.LinkUpdaterProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdater {
    private final LinkRepository linkRepository;
    private final LinkUpdaterProperties linkUpdaterProperties;
    private final ThreadPoolTaskExecutor linkUpdaterExecutor;
    private final LinkChecker linkChecker;

    public void update() {
        List<Link> links = linkRepository.findOldest(linkUpdaterProperties.getBatchSize());
        if (links.isEmpty()) return;
        List<Future<?>> futures = new ArrayList<>();
        for (Link link : links) {
            futures.add(linkUpdaterExecutor.submit(() -> {
                try {
                    linkChecker.checkLink(link);
                } catch (Exception e) {
                    log.atWarn()
                            .addKeyValue("link_url", link.getUrl())
                            .setCause(e)
                            .log("Ошибка при проверке ссылки");
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.atError().setCause(e).log("Ошибка в потоке обработки ссылок");
            }
        }
    }
}
