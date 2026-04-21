package backend.academy.linktracker.scrapper.client.dto.StackOverflow;

import java.util.List;

public record StackOverflowAnswerResponse(List<StackOverflowAnswer> items) {}
