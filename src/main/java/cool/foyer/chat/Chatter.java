package cool.foyer.chat;

import java.lang.String;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.*;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Data
public class Chatter implements CommandSender {

    @Delegate(types=CommandSender.class)
    private final ProxiedPlayer player;
    private Channel focus;
    private final Set<Channel> channels = new HashSet<>();
    @NonNull
    private Instant mutedUntil = Instant.MIN;

    public boolean muted() {
        return mutedUntil.isAfter(Instant.now());
    }

}
