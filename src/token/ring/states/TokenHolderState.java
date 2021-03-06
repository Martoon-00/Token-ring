package token.ring.states;

import misc.Colorer;
import org.apache.log4j.Logger;
import sender.listeners.ReplyProtocol;
import sender.main.DispatchType;
import sender.message.ReminderProtocol;
import token.ring.NodeContext;
import token.ring.NodeInfo;
import token.ring.NodeState;
import token.ring.Priority;
import token.ring.message.*;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class TokenHolderState extends NodeState {
    private static final Logger logger = Logger.getLogger(TokenHolderState.class);

    /**
     * At any time equals to !(IDLE_TIME time went since became Token holder) + !(computed pi) + !(currently waiting for NodeInfo)
     */
    private int stagesRemained = 2;

    private NodeInfo acceptingTokenNode;

    private final ReminderProtocol idlingTimeoutExpirationRF = ReminderProtocol.of(IdlingTimeoutExpiredReminder::new, this::onIdleTimeoutExpiration);
    private final ReminderProtocol broadcastHaveTokenRF = ReminderProtocol.of(TokenHolderTimeoutExpireReminder::new, this::onTokenHolderTimeoutExpiration);
    private ReplyProtocol[] replyProtocols = new ReplyProtocol[]{
            broadcastHaveTokenRF,
            ReplyProtocol.dumbOn(HaveTokenMsg.class, this::onHearFromOtherToken),
            idlingTimeoutExpirationRF
    };

    public TokenHolderState(NodeContext ctx) {
        super(ctx);
    }

    @Override
    public void start() {
        Arrays.stream(replyProtocols).forEach(sender::registerReplyProtocol);

        sender.remind(broadcastHaveTokenRF.newReminder(), 0);
        sender.remind(idlingTimeoutExpirationRF.newReminder(), ctx.getTimeout("holder.idle"));
        ctx.executor.submit(() -> {
            ctx.piComputator.next();
            logger.info(Colorer.format("Pi computation finished, current progress is %6`%d%`", ctx.piComputator.getCurrentPrecision()));
            markStageCompleted();
        });

        if (decideWhetherToUpdateNetMap()) {
            logger.info("Decided to gather node info");
            stagesRemained++;
            sender.broadcast(new RequestForNodeInfo(), ctx.getTimeout("holder.gather-info"),
                    (handler) -> ctx.netmap.add(handler.getMessage().nodeInfo),
                    () -> {
                        logger.info("Finished gathering node info");
                        markStageCompleted();
                    });
        }

    }

    private boolean decideWhetherToUpdateNetMap() {
        int n = ctx.netmap.size();
        return new Random().nextInt(n) == 0;
    }

    private void markStageCompleted() {
        stagesRemained--;
        if (stagesRemained == 0) {
            logger.info("All the business done, going to pass token");
            passToken();
        }
    }


    // --- passing token ---

    private void passToken() {
        printNodeInfo();

        assert ctx.netmap.size() != 0;
        if (ctx.netmap.size() == 1) {
            logger.info("No more nodes are known to give token");
            ctx.switchToState(new TokenHolderState(ctx));
        } else {
            acceptingTokenNode = ctx.netmap.getNextTo(sender.getNodeInfo());
            sender.send(new InetSocketAddress(acceptingTokenNode.address, 0), new PassTokenHandshakeMsg(), DispatchType.UDP, ctx.getTimeout("holder.handshake"),
                    handler -> passTokenStage2(handler.getMessage()),
                    this::passTokenFail
            );
        }
    }

    private void passTokenStage2(PassTokenHandshakeResponseMsg handshakeResponse) {
        logger.info("Handshake success, passing token");
        sender.send(handshakeResponse.tcpAddress, new AcceptToken(ctx.piComputator, ctx.netmap), DispatchType.TCP, ctx.getTimeout("holder.give-token"),
                handler -> {
                    logger.info("Token successfully passed");
                    ctx.switchToState(new WaiterState(ctx));
                },
                this::passTokenFail
        );
    }

    private void passTokenFail() {
        logger.info(String.format("Token passing to node %s failed. Trying again", acceptingTokenNode));
        ctx.netmap.remove(acceptingTokenNode);
        acceptingTokenNode = null;
        passToken();
    }

    private void printNodeInfo() {
        Function<NodeInfo, String> bullet = nodeInfo -> sender.getNodeInfo().equals(nodeInfo) ? "%6`-%`%6`>%`" : "%6`##%`";
        logger.trace(Colorer.format("%6`--%` Current netmap %6`--%`"));
        ctx.netmap.nodeInfos().forEach(nodeInfo -> logger.trace(String.format("   %s %s", bullet.andThen(Colorer::format).apply(nodeInfo), nodeInfo)));
        logger.trace(Colorer.format("%6`--%` netmap end     %6`--%`"));
    }


    // --- reminders & responses ---

    private void onTokenHolderTimeoutExpiration(TokenHolderTimeoutExpireReminder reminder){
        sender.broadcast(new HaveTokenMsg(ctx.getCurrentPriority()));
        sender.remind(broadcastHaveTokenRF.newReminder(), ctx.getTimeout("holder.tic"));
    }

    private void onHearFromOtherToken(HaveTokenMsg haveTokenMsg) {
        Priority ourPriority = ctx.getCurrentPriority();
        if (ourPriority.compareTo(haveTokenMsg.priority) < 0) {
            logger.info(Colorer.format("Detected token holder with %1`higher%` priority %s (our priority is %s)", haveTokenMsg.priority, ourPriority));
            ctx.switchToState(new WaiterState(ctx));
        }
    }

    private void onIdleTimeoutExpiration(IdlingTimeoutExpiredReminder reminder) {
        logger.info("Idle period passed");
        markStageCompleted();
    }

}
