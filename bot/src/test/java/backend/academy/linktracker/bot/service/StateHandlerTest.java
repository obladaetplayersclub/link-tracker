package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateManager;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class StateHandlerTest {

    @Mock
    private ScrapperClient scrapperClient;

    private UserStateManager userStateManager;
    private StateHandler stateHandler;

    private static final long CHAT_ID = 123L;

    @BeforeEach
    void setUp() {
        userStateManager = new UserStateManager();
        stateHandler = new StateHandler(userStateManager, scrapperClient);
    }

    @Test
    void handleTrackUrl_ShouldAskForTags_WhenValidUrl() {
        userStateManager.getSession(CHAT_ID).setState(UserState.AWAITING_TRACK_URL);

        SendMessage response = stateHandler.handle(CHAT_ID, "https://github.com/user/repo");

        assertThat(response.getParameters().get("text").toString()).contains("теги");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.AWAITING_TRACK_TAGS);
    }

    @Test
    void handleTrackTags_ShouldAddLink_WhenTagsProvided() {
        var session = userStateManager.getSession(CHAT_ID);
        session.setState(UserState.AWAITING_TRACK_TAGS);
        session.setTrackUrl(URI.create("https://github.com/user/repo"));

        when(scrapperClient.addLink(eq(CHAT_ID), any(AddLinkRequest.class)))
                .thenReturn(new LinkResponse(1L, URI.create("https://github.com/user/repo"), List.of("work", "java")));

        SendMessage response = stateHandler.handle(CHAT_ID, "work, java");

        assertThat(response.getParameters().get("text").toString()).contains("добавлена");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.NONE);
        verify(scrapperClient).addLink(eq(CHAT_ID), any(AddLinkRequest.class));
    }

    @Test
    void handleTrackTags_ShouldAddLink_WhenTagsSkipped() {
        var session = userStateManager.getSession(CHAT_ID);
        session.setState(UserState.AWAITING_TRACK_TAGS);
        session.setTrackUrl(URI.create("https://github.com/user/repo"));

        when(scrapperClient.addLink(eq(CHAT_ID), any(AddLinkRequest.class)))
                .thenReturn(new LinkResponse(1L, URI.create("https://github.com/user/repo"), List.of()));

        SendMessage response = stateHandler.handle(CHAT_ID, "-");

        assertThat(response.getParameters().get("text").toString()).contains("добавлена");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.NONE);
    }

    @Test
    void handleTrackUrl_ShouldRejectInvalidUrl() {
        userStateManager.getSession(CHAT_ID).setState(UserState.AWAITING_TRACK_URL);

        SendMessage response = stateHandler.handle(CHAT_ID, "not-a-valid-url");

        assertThat(response.getParameters().get("text").toString()).contains("Некорректная ссылка");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.AWAITING_TRACK_URL);
    }

    @Test
    void handleTrackUrl_ShouldRejectMalformedUrl() {
        userStateManager.getSession(CHAT_ID).setState(UserState.AWAITING_TRACK_URL);

        SendMessage response = stateHandler.handle(CHAT_ID, "not a url at all");

        assertThat(response.getParameters().get("text").toString()).contains("Некорректная ссылка");
    }

    @Test
    void handleTrackTags_ShouldRespondAlreadyTracked_WhenDuplicate() {
        var session = userStateManager.getSession(CHAT_ID);
        session.setState(UserState.AWAITING_TRACK_TAGS);
        session.setTrackUrl(URI.create("https://github.com/user/repo"));

        when(scrapperClient.addLink(eq(CHAT_ID), any(AddLinkRequest.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", null, null, null));

        SendMessage response = stateHandler.handle(CHAT_ID, "-");

        assertThat(response.getParameters().get("text").toString()).contains("уже отслеживается");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.NONE);
    }

    @Test
    void handleUntrackUrl_ShouldRemoveLink_WhenFound() {
        userStateManager.getSession(CHAT_ID).setState(UserState.AWAITING_UNTRACK_URL);

        when(scrapperClient.removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class)))
                .thenReturn(new LinkResponse(1L, URI.create("https://github.com/user/repo"), List.of()));

        SendMessage response = stateHandler.handle(CHAT_ID, "https://github.com/user/repo");

        assertThat(response.getParameters().get("text").toString()).contains("удалена");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.NONE);
    }

    @Test
    void handleUntrackUrl_ShouldRespondNotFound_WhenLinkNotTracked() {
        userStateManager.getSession(CHAT_ID).setState(UserState.AWAITING_UNTRACK_URL);

        when(scrapperClient.removeLink(eq(CHAT_ID), any(RemoveLinkRequest.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        SendMessage response = stateHandler.handle(CHAT_ID, "https://github.com/user/repo");

        assertThat(response.getParameters().get("text").toString()).contains("не найдена");
        assertThat(userStateManager.getSession(CHAT_ID).getState()).isEqualTo(UserState.NONE);
    }

    @Test
    void handleUntrackUrl_ShouldRejectInvalidUrl() {
        userStateManager.getSession(CHAT_ID).setState(UserState.AWAITING_UNTRACK_URL);

        SendMessage response = stateHandler.handle(CHAT_ID, "not a valid url");

        assertThat(response.getParameters().get("text").toString()).contains("Некорректная ссылка");
    }

    @Test
    void handle_ShouldReturnNull_WhenStateIsNone() {
        userStateManager.getSession(CHAT_ID).setState(UserState.NONE);

        SendMessage response = stateHandler.handle(CHAT_ID, "some text");

        assertThat(response).isNull();
    }
}
