package backend.academy.linktracker.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @InjectMocks
    private ListCommand listCommand;

    private static final long CHAT_ID = 123L;

    private void stubUpdate(String text) {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(message.text()).thenReturn(text);
    }

    @Test
    void handle_ShouldReturnLinksList_WhenLinksExist() {
        stubUpdate("/list");

        List<LinkResponse> links = List.of(
                new LinkResponse(1L, URI.create("https://github.com/user/repo"), List.of("work")),
                new LinkResponse(2L, URI.create("https://stackoverflow.com/questions/123/title"), List.of("java")));
        when(scrapperClient.getLinks(CHAT_ID)).thenReturn(new ListLinksResponse(links, 2));

        SendMessage response = listCommand.handle(update);

        String text = response.getParameters().get("text").toString();
        assertThat(text).contains("github.com/user/repo");
        assertThat(text).contains("stackoverflow.com/questions/123/title");
        assertThat(text).contains("[work]");
        assertThat(text).contains("[java]");
    }

    @Test
    void handle_ShouldReturnEmptyMessage_WhenNoLinks() {
        stubUpdate("/list");

        when(scrapperClient.getLinks(CHAT_ID)).thenReturn(new ListLinksResponse(List.of(), 0));

        SendMessage response = listCommand.handle(update);

        String text = response.getParameters().get("text").toString();
        assertThat(text).contains("Нет отслеживаемых ссылок");
    }

    @Test
    void handle_ShouldReturnEmptyMessage_WhenLinksAreNull() {
        stubUpdate("/list");

        when(scrapperClient.getLinks(CHAT_ID)).thenReturn(new ListLinksResponse(null, 0));

        SendMessage response = listCommand.handle(update);

        String text = response.getParameters().get("text").toString();
        assertThat(text).contains("Нет отслеживаемых ссылок");
    }

    @Test
    void handle_ShouldFilterByTag_WhenTagProvided() {
        stubUpdate("/list work");

        List<LinkResponse> links = List.of(
                new LinkResponse(1L, URI.create("https://github.com/user/repo"), List.of("work")),
                new LinkResponse(2L, URI.create("https://stackoverflow.com/questions/123/title"), List.of("personal")));
        when(scrapperClient.getLinks(CHAT_ID)).thenReturn(new ListLinksResponse(links, 2));

        SendMessage response = listCommand.handle(update);

        String text = response.getParameters().get("text").toString();
        assertThat(text).contains("github.com/user/repo");
        assertThat(text).doesNotContain("stackoverflow.com");
    }

    @Test
    void handle_ShouldReturnNoLinksWithTag_WhenNoMatchingTag() {
        stubUpdate("/list nonexistent");

        List<LinkResponse> links =
                List.of(new LinkResponse(1L, URI.create("https://github.com/user/repo"), List.of("work")));
        when(scrapperClient.getLinks(CHAT_ID)).thenReturn(new ListLinksResponse(links, 1));

        SendMessage response = listCommand.handle(update);

        String text = response.getParameters().get("text").toString();
        assertThat(text).contains("Нет ссылок с тегом");
    }
}
