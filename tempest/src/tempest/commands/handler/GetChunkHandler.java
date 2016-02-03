package tempest.commands.handler;

import tempest.commands.command.Get;
import tempest.commands.command.GetChunk;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;
import tempest.services.FileIOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by swapnalekkala on 11/1/15.
 */
public class GetChunkHandler implements ResponseCommandExecutor<GetChunk, String, String> {
    public boolean canHandle(Command.Message.Type type) {
        return type == GetChunk.type;
    }

    public Command.Message serialize(GetChunk command) {
        Command.GetChunk.Builder getChunkBuilder = Command.GetChunk.newBuilder().setRequest(command.getRequest());

        if (command.getResponse() != null)
            getChunkBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GETCHUNK)
                .setGetChunk(getChunkBuilder)
                .build();
        return message;
    }

    public GetChunk deserialize(Command.Message message) {
        GetChunk getChunk = new GetChunk();
        getChunk.setRequest(message.getGetChunk().getRequest());
        if (message.hasGetChunk() && message.getGetChunk().hasResponse())
            getChunk.setResponse(message.getGetChunk().getResponse());
        return getChunk;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {
        FileIOUtils.sendChunkFromDisk(socket, command.getRequest());
        return "Ok";
    }
}