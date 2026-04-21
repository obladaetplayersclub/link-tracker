package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.UnknownCommand;
import backend.academy.linktracker.bot.state.UserStateManager;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Test
    void process_ShouldReturnErrorMessage_WhenCommandIsUnknown() {

        Long expectedChatId = 666L;
        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.text()).thenReturn("/haiq");
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(chat.id()).thenReturn(expectedChatId);

        Command mockStartCommand = mock(Command.class);
        when(mockStartCommand.command()).thenReturn("/start");

        UnknownCommand mockUnknownCommand = mock(UnknownCommand.class);

        SendMessage expectedErrorResponse = new SendMessage(expectedChatId, "Неизвестная команда! Используйте /help");
        when(mockUnknownCommand.handle(update)).thenReturn(expectedErrorResponse);

        UserStateManager userStateManager = new UserStateManager();
        StateHandler stateHandler = mock(StateHandler.class);

        CommandDispatcher dispatcher =
                new CommandDispatcher(List.of(mockStartCommand), mockUnknownCommand, userStateManager, stateHandler);
        SendMessage response = dispatcher.process(update);

        assertThat(response.getParameters().get("chat_id")).isEqualTo(expectedChatId);

        String responseText = response.getParameters().get("text").toString();
        assertThat(responseText).contains("Неизвестная команда").contains("/help");
    }
}
