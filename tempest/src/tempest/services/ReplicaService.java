package tempest.services;

import tempest.commands.interfaces.CommandExecutor;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.sdfs.client.SDFSClient;

import java.net.Inet4Address;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 11/6/15.
 */
public class ReplicaService implements Runnable {
    private final Logger logger;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;
    private Partitioner partitioner;
    private SDFSClient sdfsClient;
    private final ScheduledExecutorService scheduler;

    public ReplicaService(Logger logger, CommandExecutor[] commandHandlers,
                          ResponseCommandExecutor[] responseCommandHandlers, Partitioner partitioner, SDFSClient sdfsClient) {
        this.logger = logger;
        scheduler = Executors.newScheduledThreadPool(1);
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
        this.partitioner = partitioner;
        this.sdfsClient = sdfsClient;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void run() {
        for (Map.Entry<String, FileReplica> entry : this.partitioner.getFileAndReplicaMap().entrySet()) {
            String sDFSFileName = this.partitioner.getsDFSFileNamesAtTheVM().get(entry.getKey());

            List<Integer> nodeKeyIds = this.partitioner.getServerListNodeIdsForChunkNoLocal(entry.getKey());
            int localNodeId = this.partitioner.getLocalMachineNodeId();

            if (!nodeKeyIds.contains(entry.getValue().getReplica1()) && !nodeKeyIds.contains(entry.getValue().getReplica2())) {
                logger.logLine(logger.INFO, "Both replica1 and replica2 died for sDFSFileName " + entry.getKey());
                byte[] byteArray = FileIOUtils.sendByteArraytoReplicate(entry.getKey());

                logger.logLine(logger.INFO, "Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)), localNodeId, nodeKeyIds.get(1), byteArray, sDFSFileName);

                logger.logLine(logger.INFO, "Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(1)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(1)), localNodeId, nodeKeyIds.get(0), byteArray, sDFSFileName);

                entry.getValue().setReplica1(nodeKeyIds.get(0));
                entry.getValue().setReplica2(nodeKeyIds.get(1));
            } else if (!nodeKeyIds.contains(entry.getValue().getReplica1()) && nodeKeyIds.contains(entry.getValue().getReplica2())) {
                logger.logLine(logger.INFO, "Only replica1 died for sDFSFileName " + entry.getKey());

                byte[] byteArray = FileIOUtils.sendByteArraytoReplicate(entry.getKey());

                for (int i = 0; i < nodeKeyIds.size(); i++) {
                    if (nodeKeyIds.get(i).equals(entry.getValue().getReplica2())) nodeKeyIds.remove(i);
                }

                logger.logLine(logger.INFO, "Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)), localNodeId, entry.getValue().getReplica2(), byteArray, sDFSFileName);

                entry.getValue().setReplica1(nodeKeyIds.get(0));
            } else if (nodeKeyIds.contains(entry.getValue().getReplica1()) && !nodeKeyIds.contains(entry.getValue().getReplica2())) {
                logger.logLine(logger.INFO,"Only replica2 died for sDFSFileName " + entry.getKey());

                byte[] byteArray = FileIOUtils.sendByteArraytoReplicate(entry.getKey());

                for (int i = 0; i < nodeKeyIds.size(); i++) {
                    if (nodeKeyIds.get(i).equals(entry.getValue().getReplica1())) nodeKeyIds.remove(i);
                }

                logger.logLine(logger.INFO, "Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)), localNodeId, entry.getValue().getReplica1(), byteArray, sDFSFileName);

                entry.getValue().setReplica2(nodeKeyIds.get(0));
            }
        }
    }
}
