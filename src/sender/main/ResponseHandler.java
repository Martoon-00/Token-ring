package sender.main;

import java.net.InetSocketAddress;

public class ResponseHandler<M extends Message> {
    private final MessageSender sender;
    private final MessageContainer<M> container;

    public ResponseHandler(MessageSender sender, MessageContainer<M> container) {
        this.sender = sender;
        this.container = container;
    }

    public M getMessage() {
        return container.message;
    }

    public InetSocketAddress getSourceAddress() {
        return container.responseListenerAddress;
    }

    public void repeateReceiving() {
        sender.rereceive(container);
    }

}
