package backend.academy.linktracker.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public abstract class AbstractChatRepositoryTest extends ComponentTest {

    @Test
    void registerChat_shouldSaveChat() {
        chatRepository.register(123L);
        assertThat(chatRepository.exists(123L)).isTrue();
    }

    @Test
    void deleteChat_shouldRemoveChat() {
        chatRepository.register(123L);
        chatRepository.delete(123L);
        assertThat(chatRepository.exists(123L)).isFalse();
    }

    @Test
    void existsChat_shouldReturnFalseForNonExistent() {
        assertThat(chatRepository.exists(999L)).isFalse();
    }
}
