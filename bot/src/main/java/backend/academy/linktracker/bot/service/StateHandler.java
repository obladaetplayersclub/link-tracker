package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.AddLinkRequest;
import backend.academy.linktracker.bot.client.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.state.UserSession;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateManager;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class StateHandler {
    private final UserStateManager userStateManager;
    private final ScrapperClient scrapperClient;

    public SendMessage handle(long chatId, String text) {
        UserSession session = userStateManager.getSession(chatId);

        return switch (session.getState()) {
            case AWAITING_TRACK_URL -> handleTrackUrl(chatId, text, session);
            case AWAITING_TRACK_TAGS -> handleTrackTags(chatId, text, session);
            case AWAITING_UNTRACK_URL -> handleUntrackUrl(chatId, text);
            case NONE -> null;
        };
    }

    private SendMessage handleTrackUrl(long chatId, String text, UserSession session) {
        URI url;
        try {
            url = URI.create(text.trim());
            if (url.getScheme() == null || url.getHost() == null) {
                return new SendMessage(chatId, "Некорректная ссылка. Попробуйте ещё раз:");
            }
        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId, "Некорректная ссылка. Попробуйте ещё раз:");
        }

        session.setTrackUrl(url);
        session.setState(UserState.AWAITING_TRACK_TAGS);
        return new SendMessage(chatId, "Введите теги через запятую или отправьте '-' для пропуска:");
    }

    private SendMessage handleTrackTags(long chatId, String text, UserSession session) {
        List<String> tags = List.of();
        if (!"-".equals(text.trim())) {
            tags = Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(session.getTrackUrl(), tags));
            userStateManager.resetSession(chatId);
            return new SendMessage(chatId, "Ссылка добавлена для отслеживания!");
        } catch (HttpClientErrorException.Conflict e) {
            userStateManager.resetSession(chatId);
            return new SendMessage(chatId, "Ссылка уже отслеживается!");
        } catch (HttpClientErrorException e) {
            userStateManager.resetSession(chatId);
            return new SendMessage(chatId, "Ошибка: " + e.getStatusCode());
        }
    }

    private SendMessage handleUntrackUrl(long chatId, String text) {
        URI url;
        try {
            url = URI.create(text.trim());
            if (url.getScheme() == null || url.getHost() == null) {
                return new SendMessage(chatId, "Некорректная ссылка. Попробуйте ещё раз:");
            }
        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId, "Некорректная ссылка. Попробуйте ещё раз:");
        }

        try {
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(url));
            userStateManager.resetSession(chatId);
            return new SendMessage(chatId, "Ссылка удалена.");
        } catch (HttpClientErrorException.NotFound e) {
            userStateManager.resetSession(chatId);
            return new SendMessage(chatId, "Ссылка не найдена.");
        } catch (HttpClientErrorException e) {
            userStateManager.resetSession(chatId);
            return new SendMessage(chatId, "Ошибка: " + e.getStatusCode());
        }
    }
}
