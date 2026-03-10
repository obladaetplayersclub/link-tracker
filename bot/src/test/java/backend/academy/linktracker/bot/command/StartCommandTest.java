package backend.academy.linktracker.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private User user;

    @Mock
    private ScrapperClient scrapperClient;

    @Test
    void handle_ShouldReturnWelcomeMessage_WithUserName() {

        StartCommand startCommand = new StartCommand(scrapperClient);
        Long expectedChatId = 666L;
        String userName = "Konstantin";

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(expectedChatId);
        when(message.from()).thenReturn(user);
        when(user.firstName()).thenReturn(userName);

        SendMessage response = startCommand.handle(update);

        assertThat(response.getParameters().get("chat_id")).isEqualTo(expectedChatId);
        assertThat(response.getParameters().get("text").toString()).contains("Добро пожаловать, " + userName);
    }
}
