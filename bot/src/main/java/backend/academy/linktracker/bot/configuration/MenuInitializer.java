package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.command.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class MenuInitializer {
    private final TelegramBot telegramBot;
    private final List<Command> commandList;

    @EventListener(ApplicationReadyEvent.class)
    public void initMenu() {
        try {
            BotCommand[] commandsArray = new BotCommand[commandList.size()];
            for (int i = 0; i < commandList.size(); i++) {
                Command command = commandList.get(i);
                String resCommandName = command.command().substring(1);
                commandsArray[i] = new BotCommand(resCommandName, command.description());
            }
            telegramBot.execute(new SetMyCommands(commandsArray));
        } catch (Exception e) {
            log.error("Ошибка инициализации меню: {}", e.getMessage());
        }
    }
}
