package cool.foyer.chat;

import java.util.Set;
import java.util.HashSet;

import lombok.experimental.Accessors;
import lombok.*;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import cool.foyer.chat.Markup;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public class Channel {

    private final String name;
    private final Set<CommandSender> recipients = new HashSet<>();

    public String format(String sender, String message) {
        return "[" + name + "] " + sender + ": " + message;
    }

    public void broadcast(String sender, String message) {
        var fmt = this.format(sender, Markup.format(message));
        for (var recipient : this.recipients) {
            recipient.sendMessage(fmt);
        }
    }

}
