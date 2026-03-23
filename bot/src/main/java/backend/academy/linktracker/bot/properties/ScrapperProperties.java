package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.scrapper")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class ScrapperProperties {

    @NotEmpty
    private String baseUrl;
}
