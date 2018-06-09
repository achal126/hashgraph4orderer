import com.dappcoder.grpc.server.ConsensusHandler;
import com.dappcoder.grpc.server.GrpcServer;
import com.swirlds.platform.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;

public class HashgraphSocketMain implements SwirldMain, ConsensusHandler {

    private GrpcServer server;

    private Platform platform;

    private Console console;

    public HashgraphSocketMain() {
        server = new GrpcServer();
    }

    @Override
    public void init(Platform platform, long l) {
        this.platform = platform;
        this.console = platform.createConsole(true);
        console.out.println("Initialized " + platform.getAddress().getSelfName());
    }

    @Override
    public void run() {
        long selfId = platform.getAddress().getId();

        int port = platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 1000;

        server.getService().addMessageHandler(this::sendAsTransaction);
        try {
            server.start(port);
            server.blockUntilShutdown();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Could not start GRPC Server", e);
        }
    }

    private boolean sendAsTransaction(String message) {
        return this.platform.createTransaction(message.getBytes());
    }

    @Override
    public void preEvent() {

    }

    @Override
    public SwirldState newState() {
        HashgraphSocketState state = new HashgraphSocketState();
        state.addConsensusHandler(server.getService());
        state.addConsensusHandler(this);
        return state;
    }

    public static void main(String[] args) {
        Browser.main(args);
    }

    @Override
    public void handle(long id, boolean consensus, Instant timestamp, byte[] transaction, Address address) {
        if (console != null) {
            try {
                console.out.println("CONSENSUS: \n" + new String(transaction, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
