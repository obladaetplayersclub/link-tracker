package backend.academy.linktracker.scrapper.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.link-updater")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class LinkUpdaterProperties {
    private int batchSize = 100;
    private int threadCount = 4;
}
