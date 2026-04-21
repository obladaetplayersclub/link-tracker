package backend.academy.linktracker.scrapper.client.dto.StackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowQuestionResponse(List<Item> items) {
    public record Item(@JsonProperty("last_activity_date") long lastActivityDate) {}
}
