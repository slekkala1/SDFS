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
 * Created by swapnalekkala on 10/27/15.
 */
public class SDFSClientApp implements Runnable {
    private final SDFSClientConsole sDFSClientConsole;
    private final FileIOUtils fileIOUtils;
    private final SDFSClient sdfsClient;

    public SDFSClientApp() throws IOException {
        String logFile = "machineSDFSClient." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        sdfsClient = new SDFSClient(logger);
        sDFSClientConsole = new SDFSClientConsole(logger, sdfsClient);
        fileIOUtils = new FileIOUtils(logger);
    }

    /**
     * Launch the command console for SDFS Client
     */
    public void run() {
        try {
            ShellFactory.createConsoleShell("SDFSClient", "", sDFSClientConsole).commandLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new SDFSClientApp().run();
    }
}
