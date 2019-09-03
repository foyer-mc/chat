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
package cool.foyer.chat.discord;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.Accessors;
import lombok.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import cool.foyer.chat.Channel;
import cool.foyer.chat.Plugin;

@RequiredArgsConstructor
public class Bot extends ListenerAdapter {

    private final Plugin plugin;
    private final String token;
    private final Map<String, Channel> discord2mc = new HashMap<>();
    private final Map<Channel, String> mc2discord = new HashMap<>();
    private JDA backend;

    public void start() throws javax.security.auth.login.LoginException {
        backend = new JDABuilder(token)
            .addEventListeners(this)
            .setActivity(Activity.playing("vec le feu"))
            .build();
    }

    public Bot addLinkedChannel(String id, Channel chan) {
        discord2mc.put(id, chan);
        mc2discord.put(chan, id);
        chan.bridge(this);
        return this;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        var chan = discord2mc.get(event.getChannel().getId());
        if (chan == null) {
            return;
        }
        chan.broadcast(event.getMember().getEffectiveName(), event.getMessage().getContentRaw(), false);
    }

    public void onInGameMessage(Channel chan, String message) {
        var id = mc2discord.get(chan);
        var guildchan = backend.getTextChannelById(id);
        guildchan.sendMessage(message).queue();
    }
}
