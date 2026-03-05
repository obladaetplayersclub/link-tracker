package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnknownCommand {
    public SendMessage handle(Update update) {
        log.info(
                "Обработка неизвестной команды для чата {}",
                update.message().chat().id());

        return new SendMessage(
                update.message().chat().id(),
                "Неизвестная команда! Используйте /help для просмотра всех имеющихся команд");
    }
}
