package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;

public interface BotNotifier {
    void sendUpdate(LinkUpdate linkUpdate);
}
