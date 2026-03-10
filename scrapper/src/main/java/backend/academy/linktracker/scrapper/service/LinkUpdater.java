package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.dto.GitHubRepoResponse;
import backend.academy.linktracker.scrapper.client.dto.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.parser.ChainLinkParser;
import backend.academy.linktracker.scrapper.parser.GitHubParsedLink;
import backend.academy.linktracker.scrapper.parser.ParsedLink;
import backend.academy.linktracker.scrapper.parser.StackOverflowParsedLink;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdater {
    private final LinkRepository linkRepository;
    private final ChainLinkParser linkParser;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final StackoverflowProperties stackoverflowProperties;
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
        var parsed = linkParser.parse(link.getUrl());
        if (parsed.isEmpty()) {
            return;
        }

        OffsetDateTime lastUpdated = fetchLastUpdated(parsed.orElseThrow());
        if (lastUpdated == null) {
            return;
        }

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

    private OffsetDateTime fetchLastUpdated(ParsedLink parsed) {
        return switch (parsed) {
            case GitHubParsedLink gh -> {
                GitHubRepoResponse response = gitHubClient.getRepository(gh.owner(), gh.repo());
                yield response.pushedAt();
            }
            case StackOverflowParsedLink so -> {
                StackOverflowQuestionResponse response = stackOverflowClient.getQuestion(
                        so.questionId(),
                        "stackoverflow",
                        stackoverflowProperties.getKey(),
                        stackoverflowProperties.getAccessToken());
                if (response.items() == null || response.items().isEmpty()) {
                    yield null;
                }
                long epoch = response.items().getFirst().lastActivityDate();
                yield OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
            }
        };
    }
}
