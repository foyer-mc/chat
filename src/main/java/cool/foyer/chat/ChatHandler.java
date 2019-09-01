package cool.foyer.chat;

import java.lang.String;

import lombok.*;

import net.md_5.bungee.api.ChatColor;
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
        chat.focus(plugin.config().getString("default_focus"));

        var defaults = plugin.config().getStringList("default_channels");
        for (var chanName : defaults) {
            var chan = this.plugin.channels().get(chanName);
            chan.recipients().add(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(PlayerDisconnectEvent event) {
        var player = event.getPlayer();
        this.plugin.channels()
            .values()
            .stream()
            .map(ch -> ch.recipients().remove(player));
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

        var player = (ProxiedPlayer) sender;
        var msg = ChatColor.stripColor(event.getMessage());
        var chat = this.plugin.chats().get(player);
        var chan = this.plugin.channels().get(chat.focus());

        if (chan == null) {
            return;
        }

        chan.broadcast(player.getName(), msg);
        event.setCancelled(true);
    }

}
