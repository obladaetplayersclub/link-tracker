package backend.academy.linktracker.scrapper.client.dto;

import java.util.List;

public record StackOverflowAnswerResponse(List<StackOverflowAnswer> items) {}
