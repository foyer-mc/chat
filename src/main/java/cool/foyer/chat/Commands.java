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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.*;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
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

    @HelpCommand
    public void help(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("switch")
    @CommandCompletion("@channels")
    public void cmdSwitch(Chatter chatter, Channel chan) {
        if (!chatter.channels().contains(chan)) {
            throw new InvalidCommandArgument(Messages.CHANNEL_NOT_JOINED, false);
        }
        chatter.focus(chan);
    }

    @Subcommand("list")
    public void cmdList(CommandSender sender) {
        this.plugin.channels().values().stream()
            .filter(ch -> sender.hasPermission(ch.permission(Channel.Permission.READ)))
            .map(ch -> "- " + ch.toString())
            .forEach(sender::sendMessage);
    }

    @Subcommand("join")
    @CommandCompletion("@channels_joinable")
    public void cmdJoin(Chatter chatter, Channel chan) {
        if (!chatter.player().hasPermission(chan.permission(Channel.Permission.READ))) {
            throw new InvalidCommandArgument(Messages.CHANNEL_NONEXISTENT, false);
        }
        chan.recipients().add(chatter.player());
        chatter.channels().add(chan);
        chatter.focus(chan);
    }

    @Subcommand("leave")
    @CommandCompletion("@channels")
    public void cmdLeave(Chatter chatter, @Optional Channel chan) {
        if (chan == null) {
            chan = chatter.focus();
        }
        chatter.channels().remove(chan);
        chan.recipients().remove(chatter.player());
        if (chan == chatter.focus()) {
            chatter.focus(plugin.defaultFocus());
        }
    }

    @Subcommand("mute")
    @CommandPermission("foyer.chat.mute")
    @CommandCompletion("@players")
    public void cmdMute(Recipient target, @Optional Duration duration) {
        if (duration == null) {
            duration = ChronoUnit.FOREVER.getDuration();
        }
        target.chatter().mutedUntil(Instant.now().plus(duration));
    }

    @Subcommand("unmute")
    @CommandPermission("foyer.chat.mute")
    @CommandCompletion("@players")
    public void cmdUnmute(Recipient target) {
        target.chatter().mutedUntil(Instant.MIN);
    }

}
