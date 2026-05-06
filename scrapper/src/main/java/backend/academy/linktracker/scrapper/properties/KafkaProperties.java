package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

    @Positive
    private int partitions = 3;

    @Positive
    private int replicationFactor = 3;
}
