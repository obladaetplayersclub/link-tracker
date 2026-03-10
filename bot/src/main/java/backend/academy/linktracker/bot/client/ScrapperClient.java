package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface ScrapperClient {

    @PostExchange("/tg-chat/{id}")
    void registerChat(@PathVariable long id);

    @DeleteExchange("/tg-chat/{id}")
    void deleteChat(@PathVariable long id);

    @GetExchange("/links")
    ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") long chatId);

    @PostExchange("/links")
    LinkResponse addLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request);

    @DeleteExchange("/links")
    LinkResponse removeLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest request);
}
