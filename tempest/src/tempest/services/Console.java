package tempest.services;

import asg.cliche.Command;
import asg.cliche.Param;
import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.interfaces.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Console provides a command console using the Cliche library
 */
public class Console {
    private final Logger logger;
    private final Client client;
    private final Server server;
    private final MembershipService membershipService;
    private final Partitioner partitioner;

    public Console(Logger logger, Client client, Server server, MembershipService membershipService, Partitioner partitioner) {
        this.logger = logger;
        this.client = client;
        this.server = server;
        this.membershipService = membershipService;
        this.partitioner = partitioner;
    }

    @Command(abbrev = "mstart")
    public void startMembership() throws UnknownHostException {
        membershipService.start(client);
    }

    @Command(abbrev = "mstop")
    public void stopMembership() {
        membershipService.stop();
    }

    @Command
    public String getMembershipList() {
        return membershipService.getMembershipList().toString();
    }

    @Command(abbrev = "id")
    public String getSelfId() {
        return membershipService.getLocalId();
    }

    @Command(abbrev = "ss")
    public void serviceStart() {
        server.start();
    }

    @Command(abbrev = "sest")
    public void serviceStop() {
        server.stop();
    }

    @Command(abbrev = "store")
    public String store() {
        return this.partitioner.getsDFSFileNamesAtTheVM().toString();
    }

    @Command(abbrev = "list")
    public String list(@Param(name = "SDFSFileName") String sDFSFileName) {
        Response<String> response = client.list(sDFSFileName);
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public void log(@Param(name = "level") String level, @Param(name = "message") String message) {
        logger.logLine(level, message);
    }

    @Command
    public String grepLocal(@Param(name = "options") String options) throws InterruptedException, IOException {
        return logger.grep(options);
    }

    @Command
    public String grepAll(@Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grepAll(options);
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String grepMachine(@Param(name = "machine", description = "host:port") String machine, @Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grep(membershipService.getMember(machine.split(":")[0], Integer.parseInt(machine.split(":")[1])), options);
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String grepLocalToFile(@Param(name = "file") String file, @Param(name = "options") String options) throws InterruptedException, IOException {
        Files.write(FileSystems.getDefault().getPath(file), logger.grep(options).getBytes());
        return "Wrote to " + file;
    }

    @Command
    public String grepAllToFile(@Param(name = "file") String file, @Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grepAll(options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote to " + file + System.getProperty("line.separator") + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String grepMachineToFile(@Param(name = "file", description = "host:port") String file, @Param(name = "machine") String machine, @Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grep(membershipService.getMember(machine.split(":")[0], Integer.parseInt(machine.split(":")[1])), options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote to " + file + System.getProperty("line.separator") + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String pingAll() throws IOException, InterruptedException {
        Response<String> response = client.pingAll();
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String pingMachine(@Param(name = "machine", description = "host:port") String machine) throws IOException, InterruptedException {
        Response<String> response = client.ping(membershipService.getMember(machine.split(":")[0], Integer.parseInt(machine.split(":")[1])));
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    private String formatResponseStatistics(ResponseData response) {
        StringBuilder resultBuilder = new StringBuilder("------------------------------");
        resultBuilder.append(System.getProperty("line.separator"));
        resultBuilder.append("Latency: ").append(response.getQueryLatency()).append("ms");
        resultBuilder.append(System.getProperty("line.separator"));
        return resultBuilder.toString();
    }


}