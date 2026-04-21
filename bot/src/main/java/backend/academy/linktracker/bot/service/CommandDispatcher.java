package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.UnknownCommand;
import backend.academy.linktracker.bot.state.UserSession;
import backend.academy.linktracker.bot.state.UserState;
import backend.academy.linktracker.bot.state.UserStateManager;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommandDispatcher {
    private final List<Command> commandList;
    private final UnknownCommand unknownCommand;
    private final UserStateManager userStateManager;
    private final StateHandler stateHandler;

    public CommandDispatcher(
            List<Command> commandList,
            UnknownCommand unknownCommand,
            UserStateManager userStateManager,
            StateHandler stateHandler) {
        this.commandList = commandList;
        this.unknownCommand = unknownCommand;
        this.userStateManager = userStateManager;
        this.stateHandler = stateHandler;
    }

    public SendMessage process(Update update) {
        if (update.message() == null || update.message().text() == null) {
            if (update.message() != null) {
                return new SendMessage(update.message().chat().id(), "Отправьте текстовую команду");
            }
            return null;
        }

        long chatId = update.message().chat().id();
        String text = update.message().text();
        UserSession session = userStateManager.getSession(chatId);

        if (text.startsWith("/")) {
            if (session.getState() != UserState.NONE) {
                userStateManager.resetSession(chatId);
            }
            for (Command command : commandList) {
                if (text.startsWith(command.command())) {
                    return command.handle(update);
                }
            }
            return unknownCommand.handle(update);
        }

        if (session.getState() != UserState.NONE) {
            return stateHandler.handle(chatId, text);
        }

        return unknownCommand.handle(update);
    }
}
