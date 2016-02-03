package tempest.commands.handler;

import tempest.commands.Response;
import tempest.commands.command.Put;
import tempest.commands.command.PutChunk;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Command;
import tempest.protos.Membership;
import tempest.services.*;

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class PutHandler implements ResponseCommandExecutor<Put, String, String> {
    private Partitioner partitioner;
    private final Logger logger;

    public PutHandler(Logger logger) {
        this.logger = logger;
    }

    public PutHandler(Logger logger, Partitioner partitioner) {
        this.logger = logger;
        this.partitioner = partitioner;
    }

    public boolean canHandle(Command.Message.Type type) {
        return type == Put.type;
    }

    public Command.Message serialize(Put command) {
        Command.Put.Builder putBuilder = Command.Put.newBuilder().setRequest(command.getRequest()).setLocalFileName(command.getLocalFileName());

        if (command.getResponse() != null)
            putBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.PUT)
                .setPut(putBuilder)
                .build();
        return message;
    }

    public Put deserialize(Command.Message message) {
        Put put = new Put();
        put.setRequest(message.getPut().getRequest());
        put.setLocalFileName(message.getPut().getLocalFileName());
        if (message.hasPut() && message.getPut().hasResponse())
            put.setResponse(message.getPut().getResponse());
        return put;
    }

    public String execute(Socket client, ResponseCommand<String, String> command) {
        byte[] fileLength = new byte[4];

        try {
            int readFile = client.getInputStream().read(fileLength);
            byte[] fileBytes = FileIOUtils.writeInputStreamToByteArray(client.getInputStream(), ByteBuffer.wrap(fileLength).getInt());
            int CHUNK_SIZE = 64000000;//6.4Mb
            chunkFile(command.getRequest(), CHUNK_SIZE, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Ok";
    }

    public void chunkFile(String sDFSFileName, int CHUNK_SIZE, byte[] sDFSFileByteArray) {
        int FILE_SIZE = sDFSFileByteArray.length;
        List<String> nameList = new ArrayList<>();

        System.out.println("Total File Size: " + FILE_SIZE);
        int chunkId = 0;
        int NUMBER_OF_CHUNKS = FILE_SIZE / CHUNK_SIZE;
        if (FILE_SIZE % CHUNK_SIZE != 0) NUMBER_OF_CHUNKS = NUMBER_OF_CHUNKS + 1;
        byte[] temporary = null;

        try {
            InputStream inStream = null;
            int totalBytesRead = 0;

            inStream = new ByteArrayInputStream(sDFSFileByteArray);

            while (totalBytesRead < FILE_SIZE) {
                String PART_NAME = sDFSFileName + chunkId + ".bin";
                int bytesRemaining = FILE_SIZE - totalBytesRead;
                if (bytesRemaining < CHUNK_SIZE) // Remaining Data Part is Smaller Than CHUNK_SIZE
                {
                    CHUNK_SIZE = bytesRemaining;
                    System.out.println("CHUNK_SIZE: " + CHUNK_SIZE);
                }
                temporary = new byte[CHUNK_SIZE]; //Temporary Byte Array
                int bytesRead = inStream.read(temporary, 0, CHUNK_SIZE);
                Response TResponse = null;
                List<Membership.Member> memberList = this.partitioner.getServerListForChunk(PART_NAME);
                List<Integer> nodeIdsForChunk = this.partitioner.getServerListNodeIdsForChunk(PART_NAME);

                boolean end;
                for (Membership.Member member : memberList) {
                    Membership.Member machine = member;
                    end = true;
                    while (end) {
                        int nodeIdOfMachine = this.partitioner.getNodeIdOfMachine(machine);
                        List<Integer> replicaList = new ArrayList<>();
                        for (Integer n : nodeIdsForChunk) {
                            if (!n.equals(nodeIdOfMachine)) {
                                replicaList.add(n);
                            }
                        }
                        System.out.println("sent " + PART_NAME + "to machine [" + machine.getPort() + ":"
                                + machine.getHost() + "] with node Id" + nodeIdOfMachine);
                        TResponse = putChunk(temporary, PART_NAME, sDFSFileName, chunkId, machine, replicaList.get(0), replicaList.get(1));
                        if (TResponse.getResponse().equals("Ok")) end = false;
                    }
                }

                if (bytesRead > 0) // If bytes read is not empty
                {
                    totalBytesRead += bytesRead;
                    chunkId++;
                }
                nameList.add(PART_NAME);
                System.out.println("Total Bytes Read: " + totalBytesRead);
            }
        } catch (IOException e) {
        }
        System.out.println(nameList.toString());
    }

    public Response putChunk(byte[] byteArray, String chunkName, String sDFSFileName, int chunkId, Membership.Member member, int replica1, int replica2) throws IOException {
        PutChunk putChunk = new PutChunk();
        putChunk.setRequest(chunkName);
        putChunk.setByteArray(byteArray);
        putChunk.setReplica1(replica1);
        putChunk.setReplica2(replica2);
        putChunk.setsDFSFileName(sDFSFileName);
        return createResponseExecutor(member, putChunk).execute();
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new PutChunkHandler(logger);
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}