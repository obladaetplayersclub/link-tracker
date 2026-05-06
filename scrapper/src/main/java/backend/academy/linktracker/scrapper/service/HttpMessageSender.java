package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport.type", havingValue = "HTTP")
public class HttpMessageSender implements MessageSender {
    private final BotClient botClient;

    @Override
    public void send(LinkUpdate linkUpdate) {
        botClient.sendUpdate(linkUpdate);
    }
}
