package backend.academy.linktracker.scrapper.client.dto.GitHub;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubRepoResponse(
        @JsonProperty("pushed_at") OffsetDateTime pushedAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
    public record GitHubIssueResponse(
            String title,
            GitHubUser user,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            String body,
            @JsonProperty("pull_request") Object pullRequest) {}
}
