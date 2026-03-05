package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.UnknownCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommandDispatcher {
    private final List<Command> commandList;
    private final UnknownCommand unknownCommand;

    public CommandDispatcher(List<Command> commandList, UnknownCommand unknownCommand) {
        this.commandList = commandList;
        this.unknownCommand = unknownCommand;
    }

    public SendMessage process(Update update) {
        if (update.message() == null || update.message().text() == null) {
            if (update.message() != null) {
                return new SendMessage(update.message().chat().id(), "Отправьте текстовую команду");
            }
            return null;
        }
        String text = update.message().text();
        for (Command command : commandList) {
            if (text.startsWith(command.command())) {
                return command.handle(update);
            }
        }
        return unknownCommand.handle(update);
    }
}
