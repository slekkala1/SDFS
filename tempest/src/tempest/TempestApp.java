package tempest;

import asg.cliche.ShellFactory;
import tempest.commands.handler.*;
import tempest.commands.interfaces.CommandExecutor;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.sdfs.client.SDFSClient;
import tempest.services.*;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * This is the entry point for Tempest and provides the lifecycle for most of the
 * players in the application.
 * <p/>
 * Currently, TempestApp is fairly clean and doing manual constructor
 * injection isn't too messy. However, if things start to get ugly, some kind
 * of IoC/DI framework may be nice if Java has a decent lightweight one.
 */
public class TempestApp implements Runnable {
    private final Console console;
    private final Server server;
    private final MembershipService membershipService;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;
    private final Partitioner partitioner;
    private final ReplicaService replicaService;
    private final SDFSClient sdfsClient;
    private final FileIOUtils fileIOUtils;

    public TempestApp() throws IOException {
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        membershipService = new MembershipService(logger);
        commandHandlers = new CommandExecutor[]{new MembershipHandler(membershipService)};
        partitioner = new Partitioner(logger, membershipService);
        responseCommandHandlers = new ResponseCommandExecutor[]{new PingHandler(), new GrepHandler(logger), new IntroduceHandler(membershipService, logger),
                new LeaveHandler(membershipService), new PutHandler(logger, partitioner), new PutChunkHandler(logger, partitioner), new GetHandler(partitioner),
                new GetChunkHandler(), new DeleteHandler(partitioner), new DeleteChunkHandler(partitioner), new ListHandler(partitioner)};
        Client client = new Client(membershipService, logger, commandHandlers, responseCommandHandlers);
        server = new Server(logger, 4444, commandHandlers, responseCommandHandlers);
        console = new Console(logger, client, server, membershipService, partitioner);
        sdfsClient = new SDFSClient(logger);
        replicaService = new ReplicaService(logger, commandHandlers, responseCommandHandlers, partitioner, sdfsClient);
        fileIOUtils = new FileIOUtils(logger);
    }

    /**
     * Start our server and launch the command console
     */
    public void run() {
        try {
            server.start();
            replicaService.start();
            ShellFactory.createConsoleShell("Tempest", "", console).commandLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new TempestApp().run();
    }
}
