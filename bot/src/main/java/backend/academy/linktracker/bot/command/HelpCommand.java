package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {
    private final List<Command> commandList;

    public HelpCommand(@Lazy List<Command> commandList) {
        this.commandList = commandList;
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Вывод списка доступных команд";
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String helpCommandLine = "**%s** — %s".formatted(command(), description());
        String otherCommands = commandList.stream()
                .filter(cmd -> !cmd.command().equals(command()))
                .map(cmd -> "**%s** — %s".formatted(cmd.command(), cmd.description()))
                .collect(Collectors.joining("\n"));
        String helpText = otherCommands.isBlank() ? helpCommandLine : helpCommandLine + "\n" + otherCommands;
        return new SendMessage(chatId, "В этом боте доступны такие команды:\n" + helpText)
                .parseMode(ParseMode.Markdown);
    }
}
