package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.client.dto.GitHubRepoResponse;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface GitHubClient {

    @GetExchange("/repos/{owner}/{repo}")
    GitHubRepoResponse getRepository(@PathVariable("owner") String owner, @PathVariable("repo") String repo);

    @GetExchange("/repos/{owner}/{repo}/issues?state=all&since={since}")
    List<GitHubIssueResponse> getIssues(
            @PathVariable String owner, @PathVariable String repo, @PathVariable String since);
}
