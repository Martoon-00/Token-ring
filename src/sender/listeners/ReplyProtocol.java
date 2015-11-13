package sender.listeners;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import sender.main.RequestMessage;
import sender.main.ResponseMessage;

import java.util.function.Function;

public interface ReplyProtocol<RequestType extends RequestMessage<ReplyType>, ReplyType extends ResponseMessage> {
    @Nullable
    ReplyType makeResponse(RequestType type);

    @NotNull
    Class<? extends RequestType> requestType();

    static <Q extends RequestMessage<A>, A extends ResponseMessage> ReplyProtocol<Q, A> of(
            Class<? extends Q> requestType,
            Function<? super Q, ? extends A> responseConstructor
    ){
        return new ReplyProtocol<Q, A>() {
            @Override
            public A makeResponse(Q q) {
                return responseConstructor.apply(q);
            }

            @Override
            public Class<? extends Q> requestType() {
                return requestType;
            }
        };
    }

}
