package tempest.networking;

import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.ServerSocket;

public class TcpServiceRunner implements Runnable {
    private final Logger logger;
    private final int port;
    private boolean isRunning = true;
    private ServerSocket server;
    private final ResponseCommandExecutor[] responseCommandHandlers;

    public TcpServiceRunner(Logger logger, int port, ResponseCommandExecutor[] responseCommandHandlers) {
        this.logger = logger;
        this.port = port;
        this.responseCommandHandlers = responseCommandHandlers;
    }

    public void run() {
        try{
            server = new ServerSocket(port);

            while(isRunning){
                TcpServiceWorker worker;
                worker = new TcpServiceWorker(server.accept(), logger, responseCommandHandlers);
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
        try {
            server.close();
        } catch (IOException ignored) {
        }
    }
}
