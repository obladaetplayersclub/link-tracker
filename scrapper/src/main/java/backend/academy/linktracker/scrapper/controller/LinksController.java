package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/links")
public class LinksController {
    private final LinkService linkService;

    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Получение списка ссылок");
        List<Link> links = linkService.findAllByChatId(chatId);
        List<LinkResponse> responses = links.stream()
                .map(link -> new LinkResponse(link.getId(), link.getUrl(), link.getTags()))
                .toList();
        return ResponseEntity.ok(new ListLinksResponse(responses, responses.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> postLinks(
            @RequestHeader("Tg-Chat-Id") long chatId, @Valid @RequestBody AddLinkRequest request) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link_url", request.link())
                .log("Добавление ссылки");
        Link link = linkService.add(chatId, request.link(), request.tags());
        return ResponseEntity.ok(new LinkResponse(link.getId(), link.getUrl(), link.getTags()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link_url", request.link())
                .log("Удаление ссылки");
        Link link = linkService.remove(chatId, request.link());
        return ResponseEntity.ok(new LinkResponse(link.getId(), link.getUrl(), link.getTags()));
    }
}
