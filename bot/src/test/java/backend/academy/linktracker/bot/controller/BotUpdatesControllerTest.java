package backend.academy.linktracker.bot.controller;

import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BotUpdatesControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @MockitoBean
    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        this.restClient =
                RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void shouldReturn200WhenValidLinkUpdate() {
        String validJson = """
            {
                "id": 1,
                "url": "https://github.com/user/repo",
                "description": "Обновление репозитория",
                "tgChatIds": [123, 456]
            }
            """;

        HttpStatusCode status = restClient
                .post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(validJson)
                .exchange((req, res) -> res.getStatusCode());

        org.junit.jupiter.api.Assertions.assertEquals(200, status.value());
    }

    @Test
    void shouldReturn400WhenInvalidLinkUpdate() {
        String invalidJson = """
            {
                "id": null,
                "url": null,
                "description": "",
                "tgChatIds": []
            }
            """;

        HttpStatusCode status = restClient
                .post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidJson)
                .exchange((req, res) -> res.getStatusCode());

        org.junit.jupiter.api.Assertions.assertEquals(400, status.value());
    }
}
