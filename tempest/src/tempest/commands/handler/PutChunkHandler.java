package tempest.commands.handler;

import tempest.commands.command.PutChunk;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.services.FileIOUtils;
import tempest.services.FileReplica;
import tempest.services.Partitioner;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by swapnalekkala on 10/29/15.
 */
public class PutChunkHandler implements ResponseCommandExecutor<PutChunk, String, String> {
    private Partitioner partitioner;
    private final Logger logger;

    public PutChunkHandler(Logger logger) {
        this.logger = logger;
    }

    public PutChunkHandler(Logger logger,Partitioner partitioner) {
        this.logger = logger;
        this.partitioner = partitioner;
    }

    public boolean canHandle(Command.Message.Type type) {
        return type == PutChunk.type;
    }

    public Command.Message serialize(PutChunk command) {
        Command.PutChunk.Builder putChunkBuilder = Command.PutChunk.newBuilder()
                .setRequest(command.getRequest())
                .setReplica1(command.getReplica1())
                .setReplica2(command.getReplica2())
                .setSDFSFileName(command.getsDFSFileName());

        if (command.getResponse() != null)
            putChunkBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.PUTCHUNK)
                .setPutChunk(putChunkBuilder)
                .build();
        return message;
    }

    public PutChunk deserialize(Command.Message message) {
        PutChunk putChunk = new PutChunk();
        putChunk.setRequest(message.getPutChunk().getRequest());
        putChunk.setReplica2(message.getPutChunk().getReplica2());
        putChunk.setReplica1(message.getPutChunk().getReplica1());
        putChunk.setsDFSFileName(message.getPutChunk().getSDFSFileName());

        if (message.hasPutChunk() && message.getPutChunk().hasResponse())
            putChunk.setResponse(message.getPutChunk().getResponse());
        return putChunk;
    }

    public String execute(Socket client, ResponseCommand<String, String> command) {

        byte[] fileLength = new byte[4];
        try {
            int readFile = client.getInputStream().read(fileLength);
            FileIOUtils.writeFileToDisk(client.getInputStream(), command.getRequest(), ByteBuffer.wrap(fileLength).getInt());
            this.partitioner.setsDFSFileNamesAtTheVM(command.getRequest(), ((PutChunk) command).getsDFSFileName());

            FileReplica fileReplica = new FileReplica(((PutChunk) command).getReplica1(), ((PutChunk) command).getReplica2());
            logger.logLine(logger.INFO, "At machine" + Inet4Address.getLocalHost().getHostName().toString() + " replica 1 at "
                    + this.partitioner.getMachineByNodeId(((PutChunk) command).getReplica1()) + " replica 2 at "
                    + this.partitioner.getMachineByNodeId(((PutChunk) command).getReplica2()));

            this.partitioner.addFileAndReplicas(command.getRequest(), fileReplica);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Ok";
    }
}