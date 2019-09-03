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

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum Messages implements MessageKeyProvider {
    CHANNEL_NONEXISTENT,
    CHANNEL_NOT_JOINED,
    INVALID_DURATION,
    NO_FOCUSED_CHANNEL,
    NO_SPEAK_PERMISSION,
    RECIPIENT_OFFLINE,
    YOU_ARE_MUTE,
    ;

    private final MessageKey key = MessageKey.of("acf-chat." + this.name().toLowerCase());

    @Override
    public MessageKey getMessageKey() {
        return key;
    }
}
