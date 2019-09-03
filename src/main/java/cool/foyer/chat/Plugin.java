package cool.foyer.chat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.experimental.Accessors;
import lombok.*;

import co.aikar.commands.BungeeCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MessageType;
import co.aikar.locales.MessageKeyProvider;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import cool.foyer.chat.ChatHandler;
import cool.foyer.chat.Commands;
import cool.foyer.chat.discord.Bot;

@Accessors(fluent = true)
@Getter
public class Plugin extends net.md_5.bungee.api.plugin.Plugin {

    private final Map<ProxiedPlayer, Chatter> chatters = new HashMap<>();
    private final Map<String, Channel> channels = new HashMap<>();
    private Configuration config;
    private BungeeCommandManager cmdManager;

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

        var discordConfig = config.getSection("discord");
        var token = discordConfig.getString("token");
        try {
            var bot = new Bot(this, token);
            var links = discordConfig.getSection("links");
            for (var name : links.getKeys()) {
                var chan = channels.get(name);
                var id = links.getString(name);
                if (chan != null && id != null) {
                    bot.addLinkedChannel(id, chan);
                }
            }
            bot.start();
        } catch (javax.security.auth.login.LoginException ex) {
            getLogger().warning("Invalid Discord token, disabling bridge");
        }


        var pm = getProxy().getPluginManager();
        pm.registerListener(this, new ChatHandler(this));

        cmdManager = new BungeeCommandManager(this);

        cmdManager.enableUnstableAPI("help");

        cmdManager.getLocales().setDefaultLocale(Locale.FRENCH);

        cmdManager.getCommandContexts().registerContext(Channel.class, c -> {
            var chan = channels.get(c.popFirstArg());
            if (chan == null) {
                throw new InvalidCommandArgument(Messages.CHANNEL_NONEXISTENT, false);
            }
            return chan;
        });

        cmdManager.getCommandContexts().registerContext(Duration.class, c -> {
            var dur = c.popFirstArg();

            var suffixes = Map.of(
                'h', ChronoUnit.HOURS,
                'm', ChronoUnit.MINUTES,
                's', ChronoUnit.SECONDS
            );

            Duration duration = Duration.ZERO;
            int start = 0;
            int pos = 0;

            try {
                for (var suffix : suffixes.entrySet()) {
                    pos = dur.indexOf(suffix.getKey(), start);
                    if (pos == -1) {
                        continue;
                    }
                    long amount = Long.parseLong(dur.substring(start, pos));
                    duration = duration.plus(amount, suffix.getValue());
                    start = pos + 1;
                }
            } catch (NumberFormatException ex) {
                throw new InvalidCommandArgument(Messages.INVALID_DURATION, "{duration}", dur.substring(start, pos));
            }
            if (start <= dur.length()) {
                throw new InvalidCommandArgument(Messages.INVALID_DURATION, "{duration}", dur);
            }

            return duration;
        });

        cmdManager.getCommandContexts().registerContext(Recipient.class, c -> {
            var name = c.getFirstArg();
            if (name == null) {
                throw new InvalidCommandArgument(true);
            }
            var player = ProxyServer.getInstance().getPlayer(c.popFirstArg());
            if (player == null) {
                throw new InvalidCommandArgument(Messages.RECIPIENT_OFFLINE, false, "{recipient}", name);
            }
            return new Recipient(chatters.get(player));
        });

        cmdManager.getCommandContexts().registerIssuerAwareContext(Chatter.class, c -> {
            var sender = c.getSender();
            var player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
            if (player == null) {
                throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
            }
            return chatters.get(player);
        });

        cmdManager.getCommandCompletions().registerCompletion("channels", c -> {
            ProxiedPlayer player = c.getIssuer().getIssuer();
            var chat = chatters.get(player);
            return chat.channels().stream()
                .filter(ch -> player.hasPermission(ch.permission(Channel.Permission.READ)))
                .map(Channel::toString)
                .collect(Collectors.toSet());
        });

        cmdManager.getCommandCompletions().registerCompletion("channels_joinable", c -> {
            ProxiedPlayer player = c.getIssuer().getIssuer();
            var chat = chatters.get(player);
            return channels.values().stream()
                .filter(ch -> player.hasPermission(ch.permission(Channel.Permission.READ)))
                .filter(ch -> !chat.channels().contains(ch))
                .map(Channel::toString)
                .collect(Collectors.toSet());
        });

        cmdManager.registerCommand(new Commands(this));
    }

    public void error(CommandSender recipient, MessageKeyProvider key, String... replacements) {
        cmdManager.sendMessage(recipient, MessageType.ERROR, key.getMessageKey(), replacements);
    }

    public void info(CommandSender recipient, MessageKeyProvider key, String... replacements) {
        cmdManager.sendMessage(recipient, MessageType.INFO, key.getMessageKey(), replacements);
    }

}
