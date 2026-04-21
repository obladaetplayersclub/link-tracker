package backend.academy.linktracker.scrapper.client.dto.StackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowUser(
        @JsonProperty("display_name") String displayName) {}
