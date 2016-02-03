package tempest.commands.handler;

import tempest.commands.Response;
import tempest.commands.command.Get;
import tempest.commands.command.GetChunk;
import tempest.commands.command.Ping;
import tempest.commands.command.PutChunk;
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

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class GetHandler implements ResponseCommandExecutor<Get, String, String> {
    private Partitioner partitioner;

    public GetHandler() {
    }

    public GetHandler(Partitioner partitioner) {
        this.partitioner = partitioner;
    }

    public boolean canHandle(Command.Message.Type type) {
        return type == Get.type;
    }

    public Command.Message serialize(Get command) {
        Command.Get.Builder getBuilder = Command.Get.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            getBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GET)
                .setGet(getBuilder)
                .build();
        return message;
    }

    public Get deserialize(Command.Message message) {
        Get get = new Get();
        get.setRequest(message.getGet().getRequest());
        if (message.hasGet() && message.getGet().hasResponse())
            get.setResponse(message.getGet().getResponse());
        return get;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {
        try {
            int value = sendFile(socket.getOutputStream(), command.getRequest());
            if (value == 0) return "file not available";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Sent the file";
    }

    public int sendFile(OutputStream outputStream, String sDFSFileName) {
        ByteArrayOutputStream AllFilesContent = null;
        int NUMBER_OF_CHUNKS = 1;
        int CURRENT_LENGTH = 0;
        AllFilesContent = new ByteArrayOutputStream();

        try {
            for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {

                List<Membership.Member> serverList = this.partitioner.getServerListForChunk(sDFSFileName + i + ".bin");
                if (serverList.isEmpty()) break;

                for (Membership.Member server : serverList) {
                    System.out.println("Get chunkName " + sDFSFileName + i + ".bin" + " from Server " + server.getHost());
                    GetChunk getChunk = getChunk(server, sDFSFileName + i + ".bin");
                    String response = getChunk.getResponse();
                    if (response.equals("Ok") && getChunk.getBytesSize() != 0) {
                        AllFilesContent.write(getChunk.getByteArray(), CURRENT_LENGTH, getChunk.getBytesSize());
                        CURRENT_LENGTH += getChunk.getBytesSize();
                        break;
                    }
                    if (getChunk.getBytesSize() == 0) {
                        outputStream.write(ByteBuffer.allocate(4).putInt(0).array());
                        return 0;
                    }
                }
            }

            AllFilesContent.flush();
            outputStream.write(ByteBuffer.allocate(4).putInt(AllFilesContent.toByteArray().length).array());
            outputStream.write(AllFilesContent.toByteArray());
            outputStream.flush();
            AllFilesContent.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Merge was executed successfully.!");
        return AllFilesContent.toByteArray().length;
    }

    public GetChunk getChunk(Membership.Member server, String chunkName) throws IOException {
        GetChunk getChunk = new GetChunk();
        getChunk.setRequest(chunkName);
        createResponseExecutor(server, getChunk).execute();
        return getChunk;
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new GetChunkHandler();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}