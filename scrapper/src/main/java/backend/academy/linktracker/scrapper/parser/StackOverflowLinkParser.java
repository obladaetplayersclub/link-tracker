package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.dto.StackOverflow.StackOverflowAnswer;
import backend.academy.linktracker.scrapper.client.dto.StackOverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkParser implements LinkParser {
    private static final int PREVIEW_MAX_LENGTH = 200;
    private final StackOverflowClient stackOverflowClient;
    private final StackoverflowProperties stackoverflowProperties;

    @Override
    public boolean supports(URI url) {
        return url.getHost() != null && url.getHost().equals("stackoverflow.com");
    }

    @Override
    public ParsedLink parse(URI url) {
        String[] segments = url.getPath().split("/");
        if (segments.length < 3 || !"questions".equals(segments[1])) {
            throw new IllegalArgumentException("Некорректная StackOverflow ссылка: " + url);
        }
        long questionId = Long.parseLong(segments[2]);
        return new StackOverflowParsedLink(questionId);
    }

    @Override
    public List<LinkUpdateInfo> checkUpdates(URI url, OffsetDateTime since) {
        StackOverflowParsedLink parsed = (StackOverflowParsedLink) parse(url);
        long fromdate = since.toEpochSecond();
        String site = "stackoverflow";
        String key = stackoverflowProperties.getKey();
        String token = stackoverflowProperties.getAccessToken();

        List<LinkUpdateInfo> updates = new ArrayList<>();

        StackOverflowAnswerResponse answers =
                stackOverflowClient.getAnswers(parsed.questionId(), site, key, token, fromdate);
        if (answers != null && answers.items() != null) {
            for (StackOverflowAnswer answer : answers.items()) {
                String author = answer.owner() != null ? answer.owner().displayName() : "unknown";
                OffsetDateTime created = toOffsetDateTime(answer.creationDate());
                updates.add(new LinkUpdateInfo("Новый ответ", author, created, truncate(answer.body())));
            }
        }

        StackOverflowAnswerResponse comments =
                stackOverflowClient.getComments(parsed.questionId(), site, key, token, fromdate);
        if (comments != null && comments.items() != null) {
            for (StackOverflowAnswer comment : comments.items()) {
                String author = comment.owner() != null ? comment.owner().displayName() : "unknown";
                OffsetDateTime created = toOffsetDateTime(comment.creationDate());
                updates.add(new LinkUpdateInfo("Новый комментарий", author, created, truncate(comment.body())));
            }
        }

        return updates;
    }

    private OffsetDateTime toOffsetDateTime(long epoch) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() <= PREVIEW_MAX_LENGTH ? text : text.substring(0, PREVIEW_MAX_LENGTH);
    }
}
