package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.dto.GitHubRepoResponse;
import java.net.URI;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubLinkParser implements LinkParser {
    private final GitHubClient gitHubClient;

    @Override
    public boolean supports(URI url) {
        return url.getHost() != null && url.getHost().equals("github.com");
    }

    @Override
    public ParsedLink parse(URI url) {
        String[] segments = url.getPath().split("/");
        if (segments.length < 3) {
            throw new IllegalArgumentException("Некорректная GitHub ссылка: " + url);
        }
        return new GitHubParsedLink(segments[1], segments[2]);
    }

    @Override
    public OffsetDateTime checkUpdate(URI url) {
        GitHubParsedLink gh = (GitHubParsedLink) parse(url);
        GitHubRepoResponse response = gitHubClient.getRepository(gh.owner(), gh.repo());
        return response.pushedAt();
    }
}
