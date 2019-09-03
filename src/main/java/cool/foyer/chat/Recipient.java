package cool.foyer.chat;

import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.*;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Data
public class Recipient {

    private final Chatter chatter;

}

