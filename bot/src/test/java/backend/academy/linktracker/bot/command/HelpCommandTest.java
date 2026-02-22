package backend.academy.linktracker.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Test
    void handle_ShouldReturnListOfCommands_WithMarkdown() {
        Long expectedChatId = 666L;
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(expectedChatId);
        Command mockStartCommand = mock(Command.class);
        when(mockStartCommand.command()).thenReturn("/start");
        when(mockStartCommand.description()).thenReturn("Запуск бота");
        HelpCommand helpCommand = new HelpCommand(List.of(mockStartCommand));
        SendMessage response = helpCommand.handle(update);
        assertThat(response.getParameters().get("chat_id")).isEqualTo(expectedChatId);
        assertThat(response.getParameters().get("parse_mode")).isEqualTo(ParseMode.Markdown);
        String responseText = response.getParameters().get("text").toString();
        assertThat(responseText)
                .startsWith("В этом боте доступны такие команды:\n")
                .contains("**/help** — Вывод списка доступных команд")
                .contains("**/start** — Запуск бота");
    }
}
