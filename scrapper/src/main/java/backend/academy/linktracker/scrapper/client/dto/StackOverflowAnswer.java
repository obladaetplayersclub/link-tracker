package backend.academy.linktracker.scrapper.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowAnswer(
    StackOverflowUser owner,
    @JsonProperty("creation_date") long creationDate,
    String body
) {}

