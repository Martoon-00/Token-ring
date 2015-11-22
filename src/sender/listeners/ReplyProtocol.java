package sender.listeners;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import sender.main.RequestMessage;
import sender.main.ResponseHandler;
import sender.main.ResponseMessage;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ReplyProtocol<RequestType extends RequestMessage<ReplyType>, ReplyType extends ResponseMessage> {

    @Nullable
    ReplyType makeResponse(ResponseHandler<RequestType> handler);

    @NotNull
    Class<? extends RequestType> requestType();

    static <Q extends RequestMessage<A>, A extends ResponseMessage> ReplyProtocol<Q, A> on(
            Class<? extends Q> requestType,
            Function<? super Q, ? extends A> responseConstructor
    ) {
        return new PlainReplyProtocol<Q, A>() {
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

    static <Q extends RequestMessage<A>, A extends ResponseMessage> ReplyProtocol<Q, A> dumbOn(
            Class<? extends Q> requestType,
            Consumer<? super Q> responseConstructor
    ) {
        return new PlainReplyProtocol<Q, A>() {
            @Override
            public A makeResponse(Q q) {
                responseConstructor.accept(q);
                return null;
            }

            @Override
            public Class<? extends Q> requestType() {
                return requestType;
            }
        };
    }


    static <Q extends RequestMessage<A>, A extends ResponseMessage> ReplyProtocol<Q, A> react(
            Class<? extends Q> requestType,
            Function<ResponseHandler<? super Q>, ? extends A> responseConstructor
    ) {
        return new ReplyProtocol<Q, A>() {
            @Override
            public A makeResponse(ResponseHandler<Q> handler) {
                return responseConstructor.apply(handler);
            }

            @Override
            public Class<? extends Q> requestType() {
                return requestType;
            }
        };
    }


    static <Q extends RequestMessage<A>, A extends ResponseMessage> ReplyProtocol<Q, A> dumbReact(
            Class<? extends Q> requestType,
            Consumer<ResponseHandler<? super Q>> responseConstructor
    ) {
        return new ReplyProtocol<Q, A>() {
            @Override
            public A makeResponse(ResponseHandler<Q> handler) {
                responseConstructor.accept(handler);
                return null;
            }

            @Override
            public Class<? extends Q> requestType() {
                return requestType;
            }
        };
    }

}
