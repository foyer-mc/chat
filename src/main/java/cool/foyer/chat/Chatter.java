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
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.*;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Data
public class Chatter implements CommandSender {

    @Delegate(types=CommandSender.class)
    private final ProxiedPlayer player;
    private Channel focus;
    private final Set<Channel> channels = new HashSet<>();
    @NonNull
    private Instant mutedUntil = Instant.MIN;

    public boolean muted() {
        return mutedUntil.isAfter(Instant.now());
    }

}
