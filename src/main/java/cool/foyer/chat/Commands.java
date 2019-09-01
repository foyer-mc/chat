package cool.foyer.chat;

import java.lang.String;

import lombok.*;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
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
        if (!chat.channels().contains(chan)) {
            throw new InvalidCommandArgument("Canal non-rejoint.", false);
        }
        chat.focus(chan);
    }

    @Subcommand("list")
    public void cmdList(CommandSender sender) {
        this.plugin.channels().values().stream()
            .filter(ch -> sender.hasPermission(ch.permission(Channel.Permission.READ)))
            .map(ch -> "- " + ch.toString())
            .forEach(sender::sendMessage);
    }

    @Subcommand("join")
    @CommandCompletion("@channels")
    public void cmdJoin(Chat chat, Channel chan) {
        if (!chat.player().hasPermission(chan.permission(Channel.Permission.READ))) {
            throw new InvalidCommandArgument("Canal non-existant.", false);
        }
        chan.recipients().add(chat.player());
        chat.channels().add(chan);
        chat.focus(chan);
    }

    @Subcommand("leave")
    @CommandCompletion("@channels")
    public void cmdLeave(Chat chat, @Optional Channel chan) {
        if (chan == null) {
            chan = chat.focus();
        }
        chat.channels().remove(chan);
        chan.recipients().remove(chat.player());
        if (chan == chat.focus()) {
            chat.focus(plugin.defaultFocus());
        }
    }

}
