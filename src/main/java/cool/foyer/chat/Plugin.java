package cool.foyer.chat;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.Accessors;
import lombok.*;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import cool.foyer.chat.ChatHandler;

@Accessors(fluent = true)
@Getter
public class Plugin extends net.md_5.bungee.api.plugin.Plugin {

    private final Map<ProxiedPlayer, Chat> chats = new HashMap<>();
    private final Map<String, Channel> channels = new HashMap<>();

    @Override
    public void onEnable() {
        channels.put("global", new Channel("global"));

        val pm = getProxy().getPluginManager();
        pm.registerListener(this, new ChatHandler(this));
    }

}
