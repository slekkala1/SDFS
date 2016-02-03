package tempest.networking;

import com.google.protobuf.InvalidProtocolBufferException;
import tempest.commands.interfaces.*;
import tempest.interfaces.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class UdpServiceWorker implements Runnable {
    private final DatagramPacket packet;
    private final DatagramSocket socket;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;
    private final Logger logger;
    private final byte[] data;
    private final Socket serverSocket = null;

    UdpServiceWorker(byte[] data, DatagramPacket packet, DatagramSocket socket, CommandExecutor[] commandHandlers, ResponseCommandExecutor[] responseCommandHandlers, Logger logger) {
        this.data = data;
        this.packet = packet;
        this.socket = socket;
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
        this.logger = logger;
    }

    public void run() {
        tempest.protos.Command.Message message;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            message = tempest.protos.Command.Message.parseDelimitedFrom(inputStream);
            for (CommandExecutor commandHandler : commandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    Command command = commandHandler.deserialize(message);
                    commandHandler.execute(command.getRequest());
                }
            }
            for (ResponseCommandExecutor commandHandler : responseCommandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    ResponseCommand command = (ResponseCommand) commandHandler.deserialize(message);
                    command.setResponse(commandHandler.execute(serverSocket, command));
                    sendResponse(commandHandler.serialize(command));
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.logLine(Logger.SEVERE, "Unable to deserialize message");
        } catch (IOException e) {
            logger.logLine(Logger.SEVERE, "Unable to deserialize message");
        }
    }

    private void sendResponse(tempest.protos.Command.Message message) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        message.writeDelimitedTo(output);
        byte[] data = output.toByteArray();
        DatagramPacket responsePacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
        try {
            socket.send(responsePacket);
        } catch (IOException e) {
            logger.logLine(Logger.WARNING, "Couldn't send response packet.");
        }
    }
}
