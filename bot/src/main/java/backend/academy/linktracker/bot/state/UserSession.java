package backend.academy.linktracker.bot.state;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private UserState state = UserState.NONE;
    private URI trackUrl;

    public void reset() {
        state = UserState.NONE;
        trackUrl = null;
    }
}
