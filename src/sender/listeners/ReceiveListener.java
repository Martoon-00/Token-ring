package sender.listeners;

import sender.main.Message;
import sender.main.MessageHandler;

@FunctionalInterface
public interface ReceiveListener<ReplyType extends Message, Handler extends MessageHandler<ReplyType>> {
    /**
     * Action performed when got an answer
     */
    void onReceive(Handler handler);
}
