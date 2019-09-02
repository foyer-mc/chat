package cool.foyer.chat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.experimental.Accessors;
import lombok.*;

import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import cool.foyer.chat.ChatHandler;
import cool.foyer.chat.Commands;

@Accessors(fluent = true)
@Getter
public class Plugin extends net.md_5.bungee.api.plugin.Plugin {

    private final Map<ProxiedPlayer, Chat> chats = new HashMap<>();
    private final Map<String, Channel> channels = new HashMap<>();
    private Configuration config;

    private String defaultTemplate;
    private List<Channel> defaultChannels = new ArrayList<>();
    private Channel defaultFocus;
    private String pmTemplate;

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

        defaultTemplate = config.getString("template");
        pmTemplate = config.getString("pm_template");
    }

    private Channel loadChannel(String name, Configuration config) {
        var template = config.getString("template", defaultTemplate);
        var color = ChatColor.valueOf(config.getString("color", "white").toUpperCase());

        return new Channel(name)
            .template(template)
            .color(color);
    }

    @Override
    public void onEnable() {
        loadConfig();

        var chansconfig = config.getSection("channels");
        for (var name : chansconfig.getKeys()) {
            channels.put(name, loadChannel(name, chansconfig.getSection(name)));
        }
        for (var name : config.getStringList("default_channels")) {
            defaultChannels.add(channels.get(name));
        }
        defaultFocus = channels.get(config.getString("default_focus"));

        var pm = getProxy().getPluginManager();
        pm.registerListener(this, new ChatHandler(this));

        var cmdManager = new BungeeCommandManager(this);

        cmdManager.getCommandContexts().registerContext(Channel.class, c -> {
            var chan = channels.get(c.popFirstArg());
            if (chan == null) {
                throw new InvalidCommandArgument("Canal non-existant.", false);
            }
            return chan;
        });

        cmdManager.getCommandContexts().registerIssuerAwareContext(Chat.class, c -> {
            var sender = c.getSender();
            var player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
            if (player == null) {
                throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
            }
            return chats.get(player);
        });

        cmdManager.getCommandCompletions().registerCompletion("channels", c -> {
            ProxiedPlayer player = c.getIssuer().getIssuer();
            var chat = chats.get(player);
            return chat.channels().stream()
                .map(Channel::toString)
                .collect(Collectors.toSet());
        });

        cmdManager.registerCommand(new Commands(this));
    }

}
