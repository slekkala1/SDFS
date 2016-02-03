package tempest.networking;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.services.DefaultLogger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpServiceWorker implements Runnable {
    private final Socket client;
    private final Logger logger;
    private final ResponseCommandExecutor[] commandHandlers;

    TcpServiceWorker(Socket client, Logger logger, ResponseCommandExecutor[] commandHandlers) {
        this.client = client;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
    }

    public void run() {
        try {
            byte[] commandLength = new byte[4];
            int read = client.getInputStream().read(commandLength);
            byte[] commandBytes = new byte[ByteBuffer.wrap(commandLength).getInt()];
            client.getInputStream().read(commandBytes);
            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(commandBytes);

            for (ResponseCommandExecutor commandHandler : commandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    ResponseCommand command = (ResponseCommand) commandHandler.deserialize(message);

                    command.setResponse(commandHandler.execute(client, command));

                    Command.Message serializedCommand = commandHandler.serialize(command);
                    serializedCommand.writeTo(client.getOutputStream());
                }
            }
            client.getOutputStream().flush();
            client.getOutputStream().close();

        } catch (IOException e) {
            e.printStackTrace();
            logger.logLine(DefaultLogger.INFO, "Error handling command" + e);
        }
    }

}



