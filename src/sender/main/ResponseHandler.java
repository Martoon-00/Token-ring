package sender.main;

import sender.listeners.ReceiveListener;

public class ResponseHandler<R extends ResponseMessage> extends MessageHandler<R> {

    public ResponseHandler(MessageSender sender, MessageContainer<R> container) {
        super(sender, container);
    }

    public <ReplyType extends ResponseMessage> void answer(
            RequestMessage<ReplyType> message, DispatchType type, int timeout,
            ReceiveListener<ReplyType, ResponseHandler<ReplyType>> receiveListener, Runnable onFail
    ) {
        sender.send(container.responseListenerAddress, message, type, timeout, receiveListener, onFail);
    }

    public <ReplyType extends ResponseMessage> void answer(
            RequestMessage<ReplyType> message, DispatchType type, int timeout,
            ReceiveListener<ReplyType, ResponseHandler<ReplyType>> receiveListener
    ) {
        sender.send(container.responseListenerAddress, message, type, timeout, receiveListener);
    }

}
