package tempest.services;

import asg.cliche.Command;
import asg.cliche.Param;
import tempest.commands.Response;
import tempest.interfaces.Logger;
import tempest.sdfs.client.SDFSClient;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class SDFSClientConsole {
    private final SDFSClient sDFSClient;
    private final Logger logger;

    public SDFSClientConsole(Logger logger, SDFSClient sDFSClient) throws IOException {
        this.logger = logger;
        this.sDFSClient = sDFSClient;
    }

    @Command
    public String put(@Param(name = "localfilename") String localFileName, @Param(name = "sdfsfilename") String sDFSFileName) {
        Response<String> response =
                sDFSClient.put(localFileName, sDFSFileName);
        if (response.getResponse().equals("Ok")) return "Successfully written to SDFS machines";
        return "Please try again put file failed";
    }

    @Command
    public String get(@Param(name = "sdfsfilename") String sDFSFileName) {
        Response<String> response =
                sDFSClient.get(sDFSFileName);
        if (response.getResponse().equals("Sent the file")) return "Successfully written " + sDFSFileName + " to disk";
        return response.getResponse();
    }

    @Command
    public String delete(@Param(name = "sdfsfilename") String sDFSFileName) {
        Response<String> response =
                sDFSClient.delete(sDFSFileName);
        if (response.getResponse().equals("Ok")) return "Successfully deleted " + sDFSFileName + " from disk";
        return "Please try again get file failed";
    }
}
