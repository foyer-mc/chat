package cool.foyer.chat;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import lombok.experimental.Accessors;
import lombok.*;

import org.apache.commons.text.StringSubstitutor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import cool.foyer.chat.Markup;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Channel {

    @EqualsAndHashCode.Include
    private final String name;
    private final Set<CommandSender> recipients = new HashSet<>();

    @NonNull
    private String template = "[${channel}] <${sender}> ${message}";
    @NonNull
    private ChatColor color = ChatColor.WHITE;

    public String format(String sender, String message) {
        var params = Map.of(
            "channel", name,
            "color", color.toString(),
            "sender", sender,
            "message", message
        );
        var subst = new StringSubstitutor(params);
        return subst.replace(template);
    }

    public void broadcast(String sender, String message) {
        var fmt = this.format(sender, Markup.format(message));
        for (var recipient : this.recipients) {
            if (recipient.hasPermission(permission(Permission.READ))) {
                recipient.sendMessage(fmt);
            }
        }
    }

    public String toString() {
        return name;
    }

    public String permission(Permission type) {
        return "foyer.chat." + name + "." + type.toString().toLowerCase();
    }

    public enum Permission {
        SPEAK,
        READ,
    }
}
