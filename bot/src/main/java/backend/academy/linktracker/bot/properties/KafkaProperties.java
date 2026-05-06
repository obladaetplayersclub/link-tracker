package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class KafkaProperties {
    @NotBlank
    private String schemaRegistryUrl;

    @NotBlank
    private String bootstrapServers;

    @NotBlank
    private String topic;

    @NotBlank
    private String groupId;

    @NotBlank
    private String dlqTopic = "link-updates.DLQ";

    @PositiveOrZero
    private int maxRetries = 3;

    @PositiveOrZero
    private long retryBackoffMs = 1000L;
}
