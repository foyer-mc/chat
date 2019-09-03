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

import java.lang.String;
import java.util.Map;

import lombok.*;

import org.apache.commons.text.StringSubstitutor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@RequiredArgsConstructor
public class ChatHandler implements Listener {

    private final Plugin plugin;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(PostLoginEvent event) {
        var player = event.getPlayer();
        var chatter = this.plugin.chatters().computeIfAbsent(player, p -> new Chatter(p));
        chatter.focus(plugin.defaultFocus());

        var defaults = plugin.defaultChannels();
        for (var chan : defaults) {
            chan.recipients().add(player);
            chatter.channels().add(chan);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogout(PlayerDisconnectEvent event) {
        var player = event.getPlayer();
        var chatter = this.plugin.chatters().get(player);
        chatter.channels()
            .stream()
            .map(ch -> ch.recipients().remove(player));
        this.plugin.chatters().remove(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(ChatEvent event) {
        var sender = event.getSender();
        if (event.isCancelled() || !(sender instanceof ProxiedPlayer)) {
            return;
        }

        // let commands go through
        if (event.isCommand()) {
            return;
        }

        event.setCancelled(true);

        var player = (ProxiedPlayer) sender;
        var msg = ChatColor.stripColor(event.getMessage());

        if (msg.charAt(0) == '@') {
            var split = msg.split(" ", 2);
            var recipientName = split[0].substring(1);
            msg = split[1];

            var recipient = ProxyServer.getInstance().getPlayer(recipientName);
            if (recipient == null) {
                plugin.error(player, Messages.RECIPIENT_OFFLINE, "{recipient}", recipientName);
            } else {
                var params = Map.of(
                    "sender", player.getName(),
                    "recipient", recipientName,
                    "message", msg
                );
                var subst = new StringSubstitutor(params);
                msg = subst.replace(this.plugin.pmTemplate());

                if (!recipient.equals(player)) {
                    recipient.sendMessage(msg);
                }
                player.sendMessage(msg);
            }
            return;
        }

        var chatter = this.plugin.chatters().get(player);
        var chan = chatter.focus();

        if (chan == null) {
            plugin.error(player, Messages.NO_FOCUSED_CHANNEL);
            return;
        }

        if (chatter.muted()) {
            plugin.error(player, Messages.YOU_ARE_MUTE);
            return;
        }

        if (!player.hasPermission(chan.permission(Channel.Permission.SPEAK))) {
            plugin.error(player, Messages.NO_SPEAK_PERMISSION, "{channel}", chan.name());
            return;
        }
        chan.broadcast(player.getName(), msg);
    }

}
