package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateManager;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {
    private final UserStateManager userStateManager;

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "Начать отслеживание ссылки";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        var session = userStateManager.getSession(chatId);
        session.setState(UserState.AWAITING_TRACK_URL);
        return new SendMessage(chatId, "Отправьте ссылку для отслеживания:");
    }
}
