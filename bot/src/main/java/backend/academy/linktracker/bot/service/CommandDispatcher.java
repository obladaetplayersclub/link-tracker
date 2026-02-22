package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommandDispatcher {
    private final List<Command> commandList;

    public CommandDispatcher(List<Command> commandList) {
        this.commandList = commandList;
    }

    public SendMessage process(Update update) {
        if (update.message() == null || update.message().text() == null) {
            if (update.message() != null) {
                return new SendMessage(update.message().chat().id(), "Отправьте текстовую команду");
            }
            return null;
        }
        String text = update.message().text();
        Long chatId = update.message().chat().id();
        for (Command command : commandList) {
            if (text.startsWith(command.command())) {
                return command.handle(update);
            }
        }
        String errorMessage = "Неизвестная команда! Используйте /help для просмотра всех имеющихся команд";
        return new SendMessage(chatId, errorMessage);
    }
}
