package bftsmart.abci;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import tendermint.abci.ABCIApplicationGrpc;
import tendermint.abci.ABCIApplicationGrpc.*;
import tendermint.abci.Types;
import tendermint.abci.Types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a class to connect BFT-SMaRt to an ABCI client that implements 'DefaultRecoverable'
 *
 * @author rSimoes
 */
public class GrpcRecoverableClient extends DefaultRecoverable {

    private final ABCIApplicationBlockingStub blockingStub;

    /**
     *
     * @param host ABCI server IP
     * @param port ABCI server port
     */
    public GrpcRecoverableClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        blockingStub = ABCIApplicationGrpc.newBlockingStub(channel);
        blockingStub.initChain(Types.RequestInitChain.newBuilder().build());
    }

    /**
     *
     * @param state The serialized snapshot
     */
    @Override
    public void installSnapshot(byte[] state) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(state);
            ObjectInputStream ois = new ObjectInputStream(bais);
            List<ByteString> chunksList = (List<ByteString>) ois.readObject();

            int index = 0;
            for(ByteString chunk : chunksList) {
                RequestApplySnapshotChunk applyChunkRequest;
                applyChunkRequest = RequestApplySnapshotChunk.newBuilder().setChunk(chunk).setIndex(index).build();
                blockingStub.applySnapshotChunk(applyChunkRequest);
                index++;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return The serialized snapshot
     */
    @Override
    public byte[] getSnapshot() {
        ResponseListSnapshots responseList = blockingStub.listSnapshots(RequestListSnapshots.newBuilder().build());
        List<Snapshot> snapshotList = responseList.getSnapshotsList();
        Snapshot maxHeightSnapshot = snapshotList.get(0);
        long maxHeight = maxHeightSnapshot.getHeight();
        for(Snapshot snapshot : snapshotList) {
            if(snapshot.getHeight() > maxHeight) {
                maxHeight = snapshot.getHeight();
                maxHeightSnapshot = snapshot;
            }
        }

        RequestLoadSnapshotChunk chunkRequest;
        ResponseLoadSnapshotChunk chunkResponse;
        List<ByteString> chunksList = new ArrayList<>();
        for(int i = 0; i < maxHeightSnapshot.getChunks(); i++) {
            chunkRequest = RequestLoadSnapshotChunk.newBuilder().setHeight(maxHeight).setChunk(i).build();
            chunkResponse = blockingStub.loadSnapshotChunk(chunkRequest);
            chunksList.add(chunkResponse.getChunk());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(chunksList);
        } catch (IOException e) {
            // Only for debug
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    /**
     *
     * @param commands The batch of requests
     * @param msgCtxs The context associated to each request
     * @param fromConsensus true if the request arrived from a consensus execution, false if it arrives from the state transfer protocol
     *
     * @return An array of serialized responses for the commands
     */
    @Override
    public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
        byte[][] replies = new byte[commands.length][];
        int index = 0;
        RequestDeliverTx request;
        ResponseDeliverTx responseCommand;

        ResponseBeginBlock responseBegin = blockingStub.beginBlock(Types.RequestBeginBlock.newBuilder().build());
        for(byte[] command : commands) {
            request = RequestDeliverTx.newBuilder().setTx(ByteString.copyFrom(command)).build();
            responseCommand = blockingStub.deliverTx(request);
            replies[index] = responseCommand.getData().toByteArray();
            index++;
        }

        ResponseEndBlock responseEnd = blockingStub.endBlock(Types.RequestEndBlock.newBuilder().build());
        ResponseCommit responseCommit = blockingStub.commit(Types.RequestCommit.newBuilder().build());
        return replies;
    }

    /**
     *
     * @param command The unordered request
     * @param msgCtx The context associated to the request
     *
     * @return The serialized response for the command
     */
    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        RequestQuery request = RequestQuery.newBuilder().setData(ByteString.copyFrom(command)).build();
        return blockingStub.query(request).getValue().toByteArray();
    }
}