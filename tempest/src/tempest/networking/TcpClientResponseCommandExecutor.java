package tempest.networking;

import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.commands.command.*;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.*;
import tempest.protos.*;
import tempest.protos.Membership;
import tempest.services.DefaultLogger;
import tempest.services.FileIOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpClientResponseCommandExecutor<TCommand extends ResponseCommand<TRequest, TResponse>, TRequest, TResponse> implements ClientResponseCommandExecutor<TResponse> {
    private final Membership.Member server;
    private final TCommand command;
    private final ResponseCommandExecutor<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public TcpClientResponseCommandExecutor(Membership.Member server, TCommand command, ResponseCommandExecutor<TCommand, TRequest, TResponse> commandHandler, Logger logger) {
        this.server = server;
        this.command = command;
        this.commandHandler = commandHandler;
        this.logger = logger;
    }

    public Response<TResponse> call() {
        return execute();
    }

    public Response<TResponse> execute() {
        long startTime = System.currentTimeMillis();
        try {
            Socket socket = new Socket(server.getHost(), server.getPort());
            Command.Message serializedCommand = commandHandler.serialize(command);
            socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(serializedCommand.toByteArray().length).array());
            serializedCommand.writeTo(socket.getOutputStream());

            if (command instanceof Put) {
                FileIOUtils.sendFile(socket, (String) ((Put) command).getLocalFileName());
            }
            if (command instanceof PutChunk) {
                FileIOUtils.sendFileChunk(socket, ((PutChunk) command).getByteArray());
            }
            socket.isOutputShutdown();
            socket.shutdownOutput();
            if (command instanceof Get) {
                byte[] commandLength = new byte[4];
                int read = socket.getInputStream().read(commandLength);
                if (ByteBuffer.wrap(commandLength).getInt() != 0) {
                    FileIOUtils.writeFileToDisk(socket.getInputStream(), (String) command.getRequest(), ByteBuffer.wrap(commandLength).getInt());
                }
            }

            if (command instanceof GetChunk) {
                byte[] fileLength = new byte[4];
                int read = socket.getInputStream().read(fileLength);
                if (ByteBuffer.wrap(fileLength).getInt() != 0) {
                    byte[] fileBytes = FileIOUtils.writeInputStreamToByteArray(socket.getInputStream(), ByteBuffer.wrap(fileLength).getInt());
                    ((GetChunk) command).setByteArray(fileBytes);
                }
                ((GetChunk) command).setBytesSize(ByteBuffer.wrap(fileLength).getInt());
            }

            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(socket.getInputStream());
            socket.close();

            ResponseCommand<TRequest, TResponse> responseCommand = commandHandler.deserialize(message);
            if (responseCommand instanceof GetChunk) {
                ((GetChunk) command).setResponse(((GetChunk) responseCommand).getResponse());
            }

            if (responseCommand instanceof DeleteChunk) {
                ((DeleteChunk) command).setResponse(((DeleteChunk) responseCommand).getResponse());
            }

            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(System.currentTimeMillis() - startTime));
            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client socket failed while connecting to [" + server + "]" + e);
            e.printStackTrace();
            return null;
        }
    }
}

