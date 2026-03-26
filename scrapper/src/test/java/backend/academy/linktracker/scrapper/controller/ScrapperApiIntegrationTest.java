package backend.academy.linktracker.scrapper.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScrapperApiIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private final RestClient restClient;

    private static final URI TEST_URL = URI.create("https://github.com/user/repo");
    private static final List<String> TEST_TAGS = List.of("work", "java");

    @Autowired
    ScrapperApiIntegrationTest(@LocalServerPort int port) {
        this.restClient =
                RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void shouldAddAndGetLink() {
        registerChat(1L);
        addLink(1L, TEST_URL, TEST_TAGS);

        ListLinksResponse response = getLinks(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(TEST_URL, response.links().getFirst().url());
    }

    @Test
    void shouldAddAndRemoveLink() {
        registerChat(1L);
        addLink(1L, TEST_URL, TEST_TAGS);

        removeLink(1L, TEST_URL);

        ListLinksResponse listResponse = getLinks(1L);
        assertEquals(0, listResponse.size());
    }

    @Test
    void shouldFailToRemoveLinkFromNonExistentChat() {
        registerChat(1L);
        addLink(1L, TEST_URL, TEST_TAGS);

        HttpStatusCode status = removeLinkExpectError(999L, TEST_URL);

        assertEquals(404, status.value());
        ListLinksResponse listResponse = getLinks(1L);
        assertEquals(1, listResponse.size());
    }

    @Test
    void shouldFailToAddLinkToNonExistentChat() {
        registerChat(1L);

        HttpStatusCode status = addLinkExpectError(2L, TEST_URL, TEST_TAGS);

        assertEquals(404, status.value());
    }

    @Test
    void shouldFailToAddLinkToDeletedChat() {
        registerChat(1L);
        deleteChat(1L);

        HttpStatusCode status = addLinkExpectError(1L, TEST_URL, TEST_TAGS);

        assertEquals(404, status.value());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentChat() {
        HttpStatusCode status = restClient.delete().uri("/tg-chat/1").exchange((req, res) -> res.getStatusCode());

        assertEquals(404, status.value());
    }

    private void registerChat(long chatId) {
        restClient.post().uri("/tg-chat/" + chatId).retrieve().toBodilessEntity();
    }

    private void deleteChat(long chatId) {
        restClient.delete().uri("/tg-chat/" + chatId).retrieve().toBodilessEntity();
    }

    private void addLink(long chatId, URI url, List<String> tags) {
        restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AddLinkRequest(url, tags))
                .retrieve()
                .body(LinkResponse.class);
    }

    private HttpStatusCode addLinkExpectError(long chatId, URI url, List<String> tags) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AddLinkRequest(url, tags))
                .exchange((req, res) -> res.getStatusCode());
    }

    private ListLinksResponse getLinks(long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponse.class);
    }

    private void removeLink(long chatId, URI url) {
        restClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RemoveLinkRequest(url))
                .retrieve()
                .body(LinkResponse.class);
    }

    private HttpStatusCode removeLinkExpectError(long chatId, URI url) {
        return restClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RemoveLinkRequest(url))
                .exchange((req, res) -> res.getStatusCode());
    }
}
