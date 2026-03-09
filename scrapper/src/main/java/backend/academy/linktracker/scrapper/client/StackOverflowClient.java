package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.client.dto.StackOverflowQuestionResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface StackOverflowClient {

    @GetExchange("/2.3/questions/{id}")
    StackOverflowQuestionResponse getQuestion(
            @PathVariable long id,
            @RequestParam String site,
            @RequestParam String key,
            @RequestParam("access_token") String accessToken);
}
