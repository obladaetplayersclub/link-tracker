package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotBlank;
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
    private String bootstrapServers;

    @NotBlank
    private String topic;

    @NotBlank
    private String groupId;
}
