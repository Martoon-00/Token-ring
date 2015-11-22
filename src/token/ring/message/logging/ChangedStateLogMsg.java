package token.ring.message.logging;

import token.ring.UniqueValue;
import token.ring.Visualizer;

public class ChangedStateLogMsg extends LogMessage {
    public final UniqueValue unique;
    public final Visualizer.State newState;

    public ChangedStateLogMsg(UniqueValue unique, Visualizer.State newState) {
        this.unique = unique;
        this.newState = newState;
    }

}
