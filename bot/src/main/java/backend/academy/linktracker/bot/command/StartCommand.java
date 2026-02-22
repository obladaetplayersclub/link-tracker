package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {
    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начало работы программы";
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        User user = update.message().from();
        String firstName = user.firstName();
        return new SendMessage(chatId, "Добро пожаловать, %s!".formatted(firstName));
    }
}
