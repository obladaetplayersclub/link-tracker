package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.state.UserStateManager;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommand implements Command {
    private final UserStateManager userStateManager;

    @Override
    public String command() {
        return "/cancel";
    }

    @Override
    public String description() {
        return "Отменить текущую операцию";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        userStateManager.resetSession(chatId);
        return new SendMessage(chatId, "Операция отменена!");
    }
}
