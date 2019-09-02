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
        var chat = this.plugin.chats().computeIfAbsent(player, p -> new Chat(p));
        chat.focus(plugin.defaultFocus());

        var defaults = plugin.defaultChannels();
        for (var chan : defaults) {
            chan.recipients().add(player);
            chat.channels().add(chan);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogout(PlayerDisconnectEvent event) {
        var player = event.getPlayer();
        var chat = this.plugin.chats().get(player);
        chat.channels()
            .stream()
            .map(ch -> ch.recipients().remove(player));
        this.plugin.chats().remove(player);
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
                player.sendMessage("§cDestinataire hors-ligne.§r");
            } else {
                var params = Map.of(
                    "sender", player.getName(),
                    "recipient", recipientName,
                    "message", msg
                );
                var subst = new StringSubstitutor(params);
                msg = subst.replace(this.plugin.pmTemplate());

                recipient.sendMessage(msg);
                player.sendMessage(msg);
            }
            return;
        }

        var chat = this.plugin.chats().get(player);
        var chan = chat.focus();

        if (chan == null) {
            player.sendMessage("§cAucun canal sélectionné.§r");
            return;
        }

        if (!player.hasPermission(chan.permission(Channel.Permission.SPEAK))) {
            player.sendMessage("§cVous n'avez pas la permission de parler sur ce canal.§r");
            return;
        }
        chan.broadcast(player.getName(), msg);
    }

}
