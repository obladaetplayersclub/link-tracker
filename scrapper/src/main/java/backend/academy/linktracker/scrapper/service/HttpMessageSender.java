package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HttpMessageSender implements MessageSender {
    private final BotClient botClient;

    @Override
    public void send(LinkUpdate linkUpdate) {
        botClient.sendUpdate(linkUpdate);
    }
}
