package bftsmart.abci;

import bftsmart.demo.counter.CounterServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

/**
 * Run this to launch a AbciClient connector. You will have to be running an ABCI compatible server application.
 *
 * @author rSimoes
 */
public class AbciClient {

    /**
     *
     * @param args - processId
     * @param args - host - client application IP
     * @param args - port - client application port
     */
    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Use: java AbciClient <processId> <host> <port>");
            System.exit(-1);
        }
        GrpcRecoverableClient grpcClient = new GrpcRecoverableClient(args[1], Integer.parseInt(args[2]));
        new ServiceReplica(Integer.parseInt(args[0]), grpcClient, grpcClient);
    }

}
