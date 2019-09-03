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
