package backend.academy.linktracker.scrapper.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubIssueResponse(
        String title,
        GitHubUser user,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        String body,
        @JsonProperty("pull_request") Object pullRequest) {}
