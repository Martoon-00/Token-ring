package sender.listeners;

import com.sun.istack.internal.Nullable;
import sender.main.RequestMessage;
import sender.main.ResponseHandler;
import sender.main.ResponseMessage;

public interface PlainReplyProtocol<RequestType extends RequestMessage<ReplyType>, ReplyType extends ResponseMessage>
        extends ReplyProtocol<RequestType, ReplyType>{

    @Nullable
    ReplyType makeResponse(RequestType type);

    @Override
    default ReplyType makeResponse(ResponseHandler<RequestType> handler) {
        return makeResponse(handler.getMessage());
    }
}
