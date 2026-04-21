package backend.academy.linktracker.scrapper.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class AppProperties {
    private int batchSize = 100;
    private int threadCount = 4;
}
