package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface BotClient extends BotNotifier {

    @Override
    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdate linkUpdate);
}
