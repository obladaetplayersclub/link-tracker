package backend.academy.linktracker.scrapper.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.dto.StackOverflow.StackOverflowAnswer;
import backend.academy.linktracker.scrapper.client.dto.StackOverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.client.dto.StackOverflow.StackOverflowUser;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackOverflowLinkParserTest {

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private StackoverflowProperties stackoverflowProperties;

    private StackOverflowLinkParser parser;

    private static final OffsetDateTime SINCE = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        parser = new StackOverflowLinkParser(stackOverflowClient, stackoverflowProperties);
    }

    private void setupProperties() {
        when(stackoverflowProperties.getKey()).thenReturn("test-key");
        when(stackoverflowProperties.getAccessToken()).thenReturn("test-token");
    }

    @Test
    void parse_shouldReturnStackOverflowParsedLink_whenValidQuestionUrl() {
        URI url = URI.create("https://stackoverflow.com/questions/12345/some-question-title");

        ParsedLink result = parser.parse(url);

        StackOverflowParsedLink parsed = assertInstanceOf(StackOverflowParsedLink.class, result);
        assertThat(parsed.questionId()).isEqualTo(12345L);
    }

    @Test
    void supports_shouldReturnFalse_whenNotStackOverflowHost() {
        assertFalse(parser.supports(URI.create("https://superuser.com/questions/12345/title")));
    }

    @Test
    void supports_shouldReturnTrue_whenStackOverflowHost() {
        assertTrue(parser.supports(URI.create("https://stackoverflow.com/questions/12345/title")));
    }

    @Test
    void parse_shouldThrow_whenPathIsNotQuestion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(URI.create("https://stackoverflow.com/users/12345/username")));
    }

    @Test
    void parse_shouldThrow_whenQuestionIdIsNotANumber() {
        assertThrows(
                NumberFormatException.class,
                () -> parser.parse(URI.create("https://stackoverflow.com/questions/abc/title")));
    }

    @Test
    void checkUpdates_shouldReturnAnswerInfo_whenNewAnswerFound() {
        setupProperties();
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");
        long creationEpoch = 1717200000L;

        when(stackOverflowClient.getAnswers(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of(
                        new StackOverflowAnswer(new StackOverflowUser("John"), creationEpoch, "Use Stream API"))));
        when(stackOverflowClient.getComments(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of()));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).hasSize(1);
        assertThat(updates.getFirst().title()).isEqualTo("Новый ответ");
        assertThat(updates.getFirst().author()).isEqualTo("John");
        assertThat(updates.getFirst().preview()).isEqualTo("Use Stream API");
    }

    @Test
    void checkUpdates_shouldReturnCommentInfo_whenNewCommentFound() {
        setupProperties();
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");
        long creationEpoch = 1717200000L;

        when(stackOverflowClient.getAnswers(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of()));
        when(stackOverflowClient.getComments(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of(
                        new StackOverflowAnswer(new StackOverflowUser("Jane"), creationEpoch, "Good question"))));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).hasSize(1);
        assertThat(updates.getFirst().title()).isEqualTo("Новый комментарий");
        assertThat(updates.getFirst().author()).isEqualTo("Jane");
        assertThat(updates.getFirst().preview()).isEqualTo("Good question");
    }

    @Test
    void checkUpdates_shouldReturnBothAnswersAndComments() {
        setupProperties();
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");
        long epoch = 1717200000L;

        when(stackOverflowClient.getAnswers(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(
                        List.of(new StackOverflowAnswer(new StackOverflowUser("User1"), epoch, "Answer text"))));
        when(stackOverflowClient.getComments(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(
                        List.of(new StackOverflowAnswer(new StackOverflowUser("User2"), epoch, "Comment text"))));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).hasSize(2);
        assertThat(updates.get(0).title()).isEqualTo("Новый ответ");
        assertThat(updates.get(1).title()).isEqualTo("Новый комментарий");
    }

    @Test
    void checkUpdates_shouldTruncatePreview_whenBodyExceeds200Chars() {
        setupProperties();
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");
        String longBody = "B".repeat(300);

        when(stackOverflowClient.getAnswers(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(
                        List.of(new StackOverflowAnswer(new StackOverflowUser("User"), 1717200000L, longBody))));
        when(stackOverflowClient.getComments(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of()));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates.getFirst().preview()).hasSize(200);
    }

    @Test
    void checkUpdates_shouldReturnEmptyList_whenNoUpdates() {
        setupProperties();
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");

        when(stackOverflowClient.getAnswers(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of()));
        when(stackOverflowClient.getComments(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of()));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates).isEmpty();
    }

    @Test
    void checkUpdates_shouldHandleNullBody() {
        setupProperties();
        URI url = URI.create("https://stackoverflow.com/questions/12345/title");

        when(stackOverflowClient.getAnswers(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(
                        List.of(new StackOverflowAnswer(new StackOverflowUser("User"), 1717200000L, null))));
        when(stackOverflowClient.getComments(eq(12345L), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new StackOverflowAnswerResponse(List.of()));

        List<LinkUpdateInfo> updates = parser.checkUpdates(url, SINCE);

        assertThat(updates.getFirst().preview()).isEmpty();
    }
}
