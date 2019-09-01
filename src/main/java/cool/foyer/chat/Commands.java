package cool.foyer.chat;

import java.lang.String;

import lombok.*;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;

import cool.foyer.chat.Plugin;

@CommandAlias("c")
public class Commands extends BaseCommand {

    private final Plugin plugin;

    public Commands(Plugin plugin) {
        super("chat");
        this.plugin = plugin;
    }

    @Subcommand("switch")
    @CommandCompletion("@channels")
    public void cmdSwitch(Chat chat, Channel chan) {
        chat.focus(chan.toString());
    }

    @Subcommand("list")
    public void cmdList(CommandSender sender) {
        this.plugin.channels().values().stream()
            .filter(ch -> sender.hasPermission(ch.permission(Channel.Permission.READ)))
            .map(ch -> "- " + ch.toString())
            .forEach(sender::sendMessage);
    }

}
