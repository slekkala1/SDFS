package tempest.networking;

import tempest.commands.interfaces.CommandExecutor;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServiceRunner implements Runnable {
    private final Logger logger;
    private final int port;
    private boolean isRunning = true;
    private DatagramSocket server;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;

    public UdpServiceRunner(Logger logger, int port, CommandExecutor[] commandHandlers, ResponseCommandExecutor[] responseCommandHandlers) {
        this.logger = logger;
        this.port = port;
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
    }

    public void run() {
        try{
            server = new DatagramSocket(port);

            while(isRunning){
                UdpServiceWorker worker;
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                server.receive(packet);
                worker = new UdpServiceWorker(data, packet, server, commandHandlers, responseCommandHandlers, logger);
                new Thread(worker).start();
            }
        } catch (IOException e) {
            if (!isRunning)
                return;
            logger.logLine(DefaultLogger.INFO, "Error accepting client request" + e);
        }
    }

    public void stop() {
        isRunning = false;
        if (server == null)
            return;
        server.close();
    }
}
