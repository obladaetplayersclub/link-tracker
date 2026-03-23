package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.dto.StackOverflowQuestionResponse;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkParser implements LinkParser {
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
    public OffsetDateTime checkUpdate(URI url) {
        StackOverflowParsedLink parsed = (StackOverflowParsedLink) parse(url);
        StackOverflowQuestionResponse response = stackOverflowClient.getQuestion(
                parsed.questionId(),
                "stackoverflow",
                stackoverflowProperties.getKey(),
                stackoverflowProperties.getAccessToken());
        if (response.items() == null || response.items().isEmpty()) {
            return null;
        }
        long epoch = response.items().getFirst().lastActivityDate();
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }
}
