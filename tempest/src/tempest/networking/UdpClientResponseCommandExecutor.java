package tempest.networking;

import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.*;
import tempest.protos.*;
import tempest.services.DefaultLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClientResponseCommandExecutor<TCommand extends ResponseCommand<TRequest, TResponse>, TRequest, TResponse> implements ClientResponseCommandExecutor<TResponse> {
    private final Membership.Member server;
    private final TCommand command;
    private final ResponseCommandExecutor<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public UdpClientResponseCommandExecutor(Membership.Member server, TCommand command, ResponseCommandExecutor<TCommand, TRequest, TResponse> commandHandler, Logger logger) {

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
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(500);

            tempest.protos.Command.Message requestMessage = commandHandler.serialize(command);
            ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
            requestMessage.writeDelimitedTo(output);
            byte[] requestData = output.toByteArray();
            DatagramPacket udpRequest = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(server.getHost()), server.getPort());
            byte[] responseData = new byte[1024];
            DatagramPacket udpResponse = new DatagramPacket(responseData, 1024);

            socket.send(udpRequest);
            socket.receive(udpResponse);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(responseData);
            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseDelimitedFrom(inputStream);
            ResponseCommand<TRequest, TResponse> responseCommand = commandHandler.deserialize(message);

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(elapsedTime));

            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client failed " + server + e);
            return null;
        }
    }
}