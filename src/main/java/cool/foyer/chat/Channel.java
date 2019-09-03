/**
 * Copyright 2019 the Foyer developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import cool.foyer.chat.discord.Bot;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Channel {

    @EqualsAndHashCode.Include
    @NonNull
    private final String name;
    private final Set<CommandSender> recipients = new HashSet<>();
    private Bot bridge;

    @NonNull
    private String template = "[${channel}] <${sender}> ${message}";
    @NonNull
    private ChatColor color = ChatColor.WHITE;

    public String format(String template, String sender, String message) {
        var params = Map.of(
            "channel", name,
            "color", color.toString(),
            "sender", sender == null ? "" : sender,
            "message", message == null ? "" : message
        );
        var subst = new StringSubstitutor(params);
        return subst.replace(template);
    }

    public String format(String sender, String message) {
        return format(template, sender, message);
    }

    public void broadcast(String sender, String message) {
        broadcast(sender, message, true);
    }

    public void broadcast(String sender, String message, boolean forwardToBridge) {
        var fmt = this.format(sender, Markup.format(message));
        for (var recipient : this.recipients) {
            if (recipient.hasPermission(permission(Permission.READ))) {
                recipient.sendMessage(fmt);
            }
        }
        if (bridge != null && forwardToBridge) {
            bridge.onInGameMessage(this, this.format("${sender}: ${message}", sender, message));
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
