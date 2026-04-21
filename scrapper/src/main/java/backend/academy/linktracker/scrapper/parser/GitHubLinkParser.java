package backend.academy.linktracker.scrapper.parser;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.dto.GitHub.GitHubRepoResponse;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubLinkParser implements LinkParser {
    private static final int PREVIEW_MAX_LENGTH = 200;
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
    public List<LinkUpdateInfo> checkUpdates(URI url, OffsetDateTime since) {
        GitHubParsedLink gh = (GitHubParsedLink) parse(url);
        List<GitHubRepoResponse.GitHubIssueResponse> issues =
                gitHubClient.getIssues(gh.owner(), gh.repo(), since.toString());

        List<LinkUpdateInfo> updates = new ArrayList<>();
        for (GitHubRepoResponse.GitHubIssueResponse issue : issues) {
            String type = issue.pullRequest() != null ? "PR" : "Issue";
            String title = type + ": " + issue.title();
            String author = issue.user() != null ? issue.user().login() : "unknown";
            String preview = truncate(issue.body());
            updates.add(new LinkUpdateInfo(title, author, issue.createdAt(), preview));
        }
        return updates;
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() <= PREVIEW_MAX_LENGTH ? text : text.substring(0, PREVIEW_MAX_LENGTH);
    }
}
