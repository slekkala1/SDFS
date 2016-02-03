package tempest.services;

import tempest.interfaces.Logger;
import tempest.protos.Membership;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by swapnalekkala on 10/30/15.
 */
public class Partitioner {

    private final List<String> allMachines = new ArrayList<String>() {{
        add("fa15-cs425-g03-01.cs.illinois.edu:4444");
        add("fa15-cs425-g03-02.cs.illinois.edu:4444");
        add("fa15-cs425-g03-03.cs.illinois.edu:4444");
        add("fa15-cs425-g03-04.cs.illinois.edu:4444");
        add("fa15-cs425-g03-05.cs.illinois.edu:4444");
        add("fa15-cs425-g03-06.cs.illinois.edu:4444");
        add("fa15-cs425-g03-07.cs.illinois.edu:4444");
    }};

    /*  private List<String> allMachines = new ArrayList<String>() {{
          add("swapnas-MacBook-Air.local:4444");
      }};
*/
    private final Logger logger;
    private final MembershipService membershipService;
    private final Map<Integer, String> allMachinesId = new HashMap<Integer, String>();
    private final Map<String, String> sDFSFileNamesAtTheVM = new HashMap<>();
    private final Map<String, FileReplica> replicas = new HashMap<String, FileReplica>();
    private final Object lock = new Object();


    public Partitioner(Logger logger, MembershipService membershipService) {
        this.membershipService = membershipService;
        this.logger = logger;
        getAllServerNodeIds();
    }

    public void addFileAndReplicas(String sDFSFileChunkName, FileReplica fileReplica) {
        synchronized (lock) {
            this.replicas.put(sDFSFileChunkName, fileReplica);
            logger.logLine(logger.INFO, "added replica information for " + sDFSFileChunkName);
        }
    }

    public Map<String, FileReplica> getFileAndReplicaMap() {
        synchronized (lock) {
            return this.replicas;
        }
    }

    public void getAllServerNodeIds() {
        for (int i = 0; i < allMachines.size(); i++) {
            allMachinesId.put(HashKey.hexToKey(HashKey.hashKey(allMachines.get(i))),
                    allMachines.get(i));
        }
    }

    public Map<String, String> getsDFSFileNamesAtTheVM() {
        return this.sDFSFileNamesAtTheVM;
    }

    public String getLocalHostName() {
        try {
            return Inet4Address.getLocalHost().getHostName().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setsDFSFileNamesAtTheVM(String addedSDFSFile, String sDFSFileName) {
        this.sDFSFileNamesAtTheVM.put(addedSDFSFile, sDFSFileName);
        logger.logLine(logger.INFO, "added " + sDFSFileName + " to stored files at the machine");
    }

    public String getMachineByNodeId(int nodeId) {
        return allMachinesId.get(nodeId);
    }

    public int getNodeIdOfMachine(Membership.Member member) {
        return HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort()));
    }

    public int getLocalMachineNodeId() {
        int localNodeId = HashKey.hexToKey(HashKey.hashKey(getLocalHostName() + ":4444"));
        return localNodeId;
    }


    public List<Membership.Member> getServerListForChunk(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        List<Integer> nodeKeyIds = new ArrayList<Integer>();

        Collections.sort(aliveIds);
        for (int i = 0; i < aliveIds.size(); i++) {
            if (fileKey <= aliveIds.get(i)) {
                nodeKeyIds.add(aliveIds.get(i));

                if (aliveIds.size() >= 2) {
                    if (i + 1 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 1 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 1));
                    }
                }
                if (aliveIds.size() >= 3) {
                    if (i + 2 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 2 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 2));
                    }
                }
                break;
            }
        }

        if (nodeKeyIds.isEmpty() && aliveIds.size() >= 3) {
            nodeKeyIds.add(aliveIds.get(0));
            nodeKeyIds.add(aliveIds.get(1));
            nodeKeyIds.add(aliveIds.get(2));
        }


        List<Membership.Member> memberList = new ArrayList<>();
        if (!nodeKeyIds.isEmpty()) {
            Membership.Member member1 = Membership.Member.newBuilder().
                    setPort(Integer.parseInt(allMachinesId.get(nodeKeyIds.get(0)).split(":")[1]))
                    .setHost(allMachinesId.get(nodeKeyIds.get(0)).split(":")[0]).build();
            memberList.add(member1);
        }
        if (nodeKeyIds.size() >= 2) {
            Membership.Member member2 = Membership.Member.newBuilder().
                    setPort(Integer.parseInt(allMachinesId.get(nodeKeyIds.get(1)).split(":")[1]))
                    .setHost(allMachinesId.get(nodeKeyIds.get(1)).split(":")[0]).build();
            memberList.add(member2);

        }
        if (nodeKeyIds.size() >= 3) {
            Membership.Member member3 = Membership.Member.newBuilder().
                    setPort(Integer.parseInt(allMachinesId.get(nodeKeyIds.get(2)).split(":")[1]))
                    .setHost(allMachinesId.get(nodeKeyIds.get(2)).split(":")[0]).build();
            memberList.add(member3);
        }

        return memberList;
    }

    public List<Integer> getServerListNodeIdsForChunk(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        List<Integer> nodeKeyIds = new ArrayList<Integer>();

        Collections.sort(aliveIds);
        for (int i = 0; i < aliveIds.size(); i++) {
            if (fileKey <= aliveIds.get(i)) {
                nodeKeyIds.add(aliveIds.get(i));

                if (aliveIds.size() >= 2) {
                    if (i + 1 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 1 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 1));
                    }
                }
                if (aliveIds.size() >= 3) {
                    if (i + 2 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 2 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 2));
                    }
                }
                break;
            }
        }

        if (nodeKeyIds.isEmpty() && aliveIds.size() >= 3) {
            nodeKeyIds.add(aliveIds.get(0));
            nodeKeyIds.add(aliveIds.get(1));
            nodeKeyIds.add(aliveIds.get(2));
        }

        return nodeKeyIds;
    }

    public List<Integer> getServerListNodeIdsForChunkNoLocal(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        List<Integer> nodeKeyIds = new ArrayList<Integer>();

        Collections.sort(aliveIds);
        for (int i = 0; i < aliveIds.size(); i++) {
            if (fileKey <= aliveIds.get(i)) {
                nodeKeyIds.add(aliveIds.get(i));

                if (aliveIds.size() >= 2) {
                    if (i + 1 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 1 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 1));
                    }
                }
                if (aliveIds.size() >= 3) {
                    if (i + 2 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 2 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 2));
                    }
                }
                break;
            }
        }

        if (nodeKeyIds.isEmpty() && aliveIds.size() >= 3) {
            nodeKeyIds.add(aliveIds.get(0));
            nodeKeyIds.add(aliveIds.get(1));
            nodeKeyIds.add(aliveIds.get(2));
        }

        int localNodeId = getLocalMachineNodeId();
        for (int i = 0; i < nodeKeyIds.size(); i++) {
            if (nodeKeyIds.get(i).equals(localNodeId)) nodeKeyIds.remove(i);
        }

        return nodeKeyIds;
    }
}
