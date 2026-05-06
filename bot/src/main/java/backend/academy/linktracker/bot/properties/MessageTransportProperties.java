package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.message-transport")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class MessageTransportProperties {
    public enum Type {
        HTTP,
        KAFKA
    }

    @NotNull
    private Type type = Type.KAFKA;
}
