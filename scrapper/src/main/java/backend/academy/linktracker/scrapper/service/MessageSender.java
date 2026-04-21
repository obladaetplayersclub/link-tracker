package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;

public interface MessageSender {
    void send(LinkUpdate linkUpdate);
}
