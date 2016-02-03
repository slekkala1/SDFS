package tempest.commands.handler;

import tempest.commands.command.Delete;
import tempest.commands.command.DeleteChunk;
import tempest.commands.command.GetChunk;
import tempest.commands.command.Ping;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Command;
import tempest.protos.Membership;
import tempest.services.CommandLineExecutor;
import tempest.services.DefaultLogWrapper;
import tempest.services.DefaultLogger;
import tempest.services.Partitioner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.List;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class DeleteHandler implements ResponseCommandExecutor<Delete, String, String> {
    private Partitioner partitioner;

    public DeleteHandler() {
    }

    public DeleteHandler(Partitioner partitioner) {
        this.partitioner = partitioner;
    }

    public boolean canHandle(Command.Message.Type type) {
        return type == Delete.type;
    }

    public Command.Message serialize(Delete command) {
        Command.Delete.Builder deleteBuilder = Command.Delete.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            deleteBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.DELETE)
                .setDelete(deleteBuilder)
                .build();
        return message;
    }

    public Delete deserialize(Command.Message message) {
        Delete delete = new Delete();
        delete.setRequest(message.getDelete().getRequest());
        if (message.hasDelete() && message.getDelete().hasResponse())
            delete.setResponse(message.getDelete().getResponse());
        return delete;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {
        deleteFiles(command.getRequest());
        return "Ok";
    }

    public void deleteFiles(String sDFSFileName) {
        int NUMBER_OF_CHUNKS = 1;

        try {
            for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
                String chunkName = sDFSFileName + i + ".bin";
                List<Membership.Member> serverList = this.partitioner.getServerListForChunk(sDFSFileName + i + ".bin");
                for (Membership.Member server : serverList) {
                    deleteChunk(server, chunkName);
                }
            }
        } catch (IOException e) {
        }
    }

    public DeleteChunk deleteChunk(Membership.Member server, String chunkName) throws IOException {
        DeleteChunk deleteChunk = new DeleteChunk();
        deleteChunk.setRequest(chunkName);
        createResponseExecutor(server, deleteChunk).execute();
        return deleteChunk;
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new DeleteChunkHandler();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}