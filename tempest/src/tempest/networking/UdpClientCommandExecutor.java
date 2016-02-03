package tempest.networking;

import tempest.commands.interfaces.Command;
import tempest.commands.interfaces.CommandExecutor;
import tempest.interfaces.ClientCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Membership;
import tempest.services.DefaultLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClientCommandExecutor<TCommand extends Command<TRequest>, TRequest> implements ClientCommandExecutor {
    private final Membership.Member server;
    private final TCommand command;
    private final CommandExecutor<TCommand, TRequest> commandHandler;
    private final Logger logger;

    public UdpClientCommandExecutor(Membership.Member server, TCommand command, CommandExecutor<TCommand, TRequest> commandHandler, Logger logger) {

        this.server = server;
        this.command = command;
        this.commandHandler = commandHandler;
        this.logger = logger;
    }

    public Object call() {
        execute();
        return null;
    }

    public void execute() {
        try {
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(500);

            tempest.protos.Command.Message message = commandHandler.serialize(command);
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            message.writeDelimitedTo(output);
            byte[] requestData = output.toByteArray();
            DatagramPacket udpRequest = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(server.getHost()), server.getPort());
            socket.send(udpRequest);

        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client failed " + server + e);
        }
    }
}