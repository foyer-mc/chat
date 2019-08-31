package cool.foyer.chat;

import java.lang.String;

import lombok.experimental.Accessors;
import lombok.*;

import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Data
public class Chat {

    private final ProxiedPlayer player;
    private String focus = "global";

}
