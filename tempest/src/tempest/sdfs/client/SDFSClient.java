package tempest.sdfs.client;

import tempest.commands.Response;
import tempest.commands.command.*;
import tempest.commands.handler.*;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.commands.interfaces.Udp;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.networking.UdpClientResponseCommandExecutor;
import tempest.protos.Membership;
import tempest.services.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class SDFSClient {


    private List<String> allMachines = new ArrayList<String>() {{
        add("fa15-cs425-g03-01.cs.illinois.edu:4444");
        add("fa15-cs425-g03-02.cs.illinois.edu:4444");
        add("fa15-cs425-g03-03.cs.illinois.edu:4444");
        add("fa15-cs425-g03-04.cs.illinois.edu:4444");
        add("fa15-cs425-g03-05.cs.illinois.edu:4444");
        add("fa15-cs425-g03-06.cs.illinois.edu:4444");
        add("fa15-cs425-g03-07.cs.illinois.edu:4444");
    }};

    private static ExecutorService pool = Executors.newFixedThreadPool(7);

    /*     private List<String> allMachines = new ArrayList<String>() {{
             add("swapnas-MacBook-Air.local:4444");
         }};
    */
    private ResponseCommandExecutor[] responseCommandHandlers;
    private final Logger logger;

    public String getRandomMachine() {
        int idx = new Random().nextInt(allMachines.size());
        String randomMachine = allMachines.get(idx);
        return randomMachine;
    }

    public SDFSClient(Logger logger) throws IOException {
        this.logger = logger;
        responseCommandHandlers = new ResponseCommandExecutor[]{new GetHandler(), new PutHandler(logger), new DeleteHandler(), new PutChunkHandler(logger)};
    }

    public Response delete(String sDFSFileName) {
        Delete delete = new Delete();
        delete.setRequest(sDFSFileName);
        Response response = null;
        while (response == null) {
            String randomMachine = getRandomMachine();
            Membership.Member member = Membership.Member.newBuilder()
                    .setHost(randomMachine.split(":")[0])
                    .setPort(Integer.parseInt(randomMachine.split(":")[1]))
                    .build();
            response = createResponseExecutor(member, delete).execute();
        }
        return response;
    }

    public Response get(String sDFSFileName) {
        Get get = new Get();
        get.setRequest(sDFSFileName);
        Response response = null;
        long startTime = System.currentTimeMillis();

        while (response == null) {
            String randomMachine = getRandomMachine();
            Membership.Member member = Membership.Member.newBuilder()
                    .setHost(randomMachine.split(":")[0])
                    .setPort(Integer.parseInt(randomMachine.split(":")[1]))
                    .build();
            response = createResponseExecutor(member, get).execute();

        }
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("time taken for get to complete " + timeTaken);

        return response;
    }

    public Response put(String localFileName, String sdfsFileName) {
        Put put = new Put();
        put.setRequest(sdfsFileName);
        put.setLocalFileName(localFileName);
        Response response = null;

        long startTime = System.currentTimeMillis();

        while (response == null) {
            String randomMachine = getRandomMachine();
            Membership.Member member = Membership.Member.newBuilder()
                    .setHost(randomMachine.split(":")[0])
                    .setPort(Integer.parseInt(randomMachine.split(":")[1]))
                    .build();

            response = createResponseExecutor(member, put).execute();
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("time taken for put to complete " + timeTaken);

        return response;
    }

    public Response replicate(String chunkFileName, String machine, int replica1, int replica2, byte[] byteArray, String sDFSFileName) {
        PutChunk putChunk = new PutChunk();
        putChunk.setRequest(chunkFileName);
        putChunk.setReplica1(replica1);
        putChunk.setReplica2(replica2);
        putChunk.setByteArray(byteArray);
        putChunk.setsDFSFileName(sDFSFileName);

        long startTime = System.currentTimeMillis();

        Membership.Member member = Membership.Member.newBuilder()
                .setHost(machine.split(":")[0])
                .setPort(Integer.parseInt(machine.split(":")[1]))
                .build();
        Response response = createResponseExecutor(member, putChunk).execute();
        long timeTaken = System.currentTimeMillis() - startTime;

        System.out.println("time taken for replicate to complete " + timeTaken);

        return response;
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
        ResponseCommandExecutor commandHandler = null;
        for (ResponseCommandExecutor ch : responseCommandHandlers) {
            if (ch.canHandle(command.getType()))
                commandHandler = ch;
        }
        if (command instanceof Udp)
            return new UdpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }

    private <TRequest, TResponse> Response<TResponse> executeAllParallel(ResponseCommand<TRequest, TResponse> responseCommand, List<Membership.Member> memberList) {
        Collection<Callable<Response<TResponse>>> commandExecutors = new ArrayList<>();

        for (Membership.Member machine : memberList)
            commandExecutors.add(createResponseExecutor(machine, responseCommand));

        List<Future<Response<TResponse>>> results;
        try {
            results = pool.invokeAll(commandExecutors);
            Response<TResponse> response = null;
            for (Future<Response<TResponse>> future : results) {
                try {
                    if (response == null)
                        response = future.get();
                    else {
                        Response<TResponse> tResponse = future.get();
                        if (tResponse != null) {
                            response.setResponseData(response.getResponseData().add(tResponse.getResponseData()));
                            response.setResponse(responseCommand.add(response.getResponse(), tResponse.getResponse()));
                        }
                    }
                } catch (ExecutionException e) {
                    logger.logLine(DefaultLogger.SEVERE, String.valueOf(e));
                }
            }
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}

