package cool.foyer.chat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import lombok.experimental.Accessors;
import lombok.*;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import cool.foyer.chat.ChatHandler;

@Accessors(fluent = true)
@Getter
public class Plugin extends net.md_5.bungee.api.plugin.Plugin {

    private final Map<ProxiedPlayer, Chat> chats = new HashMap<>();
    private final Map<String, Channel> channels = new HashMap<>();
    private Configuration config;

    private void loadConfig() {
        var datadir = getDataFolder();
        var file = new File(datadir, "config.yml");
        if (!file.exists()) {
            datadir.mkdirs();
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider
                .getProvider(YamlConfiguration.class)
                .load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        loadConfig();

        var chanconfig = config.getSection("channels");
        for (var name : chanconfig.getKeys()) {
            channels.put(name, new Channel(name));
        }

        var pm = getProxy().getPluginManager();
        pm.registerListener(this, new ChatHandler(this));
    }

}
