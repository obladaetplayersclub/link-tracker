package backend.academy.linktracker.scrapper.client.dto.StackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowAnswer(
        StackOverflowUser owner,
        @JsonProperty("creation_date") long creationDate,
        String body) {}
