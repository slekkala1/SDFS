package tempest.services;

import tempest.commands.Response;
import tempest.interfaces.Logger;
import tempest.protos.Membership;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * MembershipService is the main component of our gossip style
 * membership implementation.
 * <p/>
 * It's main function is to provide a wrapper around a list of
 * MemberHealth. There is a MemberHealth to represent each known
 * member of our group.  MembershipService keeps this list up to date
 * based on gossips that it receives from other members of the group.
 * Upon request MembershipService provides the authoritative
 * membership list for this member.
 * <p/>
 * Methods which access or change the list of MemberHealths are
 * synchronized to avoid threading issues since Server is multi threaded
 * and receives random updates to the membership list.
 */

public class MembershipService {
    private final Logger logger;
    private final String introducer;
    private final int localPort;
    private MemberHealth localMemberHealth;
    private Heartbeat heartbeat;
    private Client client;
    private String master;
    private final List<MemberHealth> memberHealths = new ArrayList<>();

    public MembershipService(Logger logger) throws UnknownHostException {
        this(logger, readPropertiesFile(), 4444);
    }

    public MembershipService(Logger logger, String introducer, int localPort) throws UnknownHostException {
        this.logger = logger;
        this.introducer = introducer;
        //this.introducer = "localhost:4444";
        this.localPort = localPort;
        localMemberHealth = new MemberHealth(Inet4Address.getLocalHost().getHostName(), localPort, System.currentTimeMillis(), 0);
    }

    /**
     * start introduces this member to the introducer and starts a Heartbeat that periodically
     * sends the current membership list to a random member of the membership list.
     *
     * @param client
     * @throws UnknownHostException
     */
    public void start(Client client) throws UnknownHostException {
        this.client = client;
        this.heartbeat = new Heartbeat(client, this);
        Membership.Member introduceMember = Membership.Member.newBuilder()
                .setHost(introducer.split(":")[0])
                .setPort(Integer.parseInt(introducer.split(":")[1]))
                .build();
        localMemberHealth = new MemberHealth(Inet4Address.getLocalHost().getHostName(), localPort, System.currentTimeMillis(), 0);
        Response<Membership.MembershipList> introduceResponse = client.introduce(introduceMember, localMemberHealth.toMember());
        merge(introduceResponse.getResponse());
        heartbeat.start();
    }

    public void stop() {
        heartbeat.stop();
        client.leave(localMemberHealth.toMember());
        memberHealths.clear();
    }

    public synchronized void addMember(Membership.Member member) {
        if (localMemberHealth.matches(member)) {
            return;
        }
        MemberHealth memberHealth = new MemberHealth(member);
        memberHealths.add(memberHealth);
        logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has joined");
    }

    public synchronized void memberLeft(Membership.Member memberLeft) {
        for (MemberHealth memberHealth : memberHealths) {
            if (memberHealth.matches(memberLeft)) {
                memberHealth.setHasLeft(true);
                logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has left");
            }
        }
    }

    /**
     * merges a given membership list with our list of MemberHealths and updates them to
     * reflect the newest known states.
     *
     * @param membershipList
     */
    public synchronized void merge(Membership.MembershipList membershipList) {
        for (Membership.Member member : membershipList.getMemberList()) {
            if (localMemberHealth.matches(member)) {
                continue;
            }
            boolean merged = false;
            for (MemberHealth memberHealth : memberHealths) {
                if (memberHealth.matches(member)) {
                    memberHealth.merge(member);
                    merged = true;
                    continue;
                }
            }
            if (!merged) {
                MemberHealth memberHealth = new MemberHealth(member);
                memberHealths.add(memberHealth);
                logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has joined");
            }
        }
    }

    /**
     * builds an immutable membership list that reflects the current state of our list
     * of MemberHealths
     *
     * @return
     */
    public synchronized Membership.MembershipList getMembershipList() {
        Membership.MembershipList.Builder builder = Membership.MembershipList.newBuilder().addMember(localMemberHealth.toMember());
        for (MemberHealth memberHealth : memberHealths) {
            if (!memberHealth.hasLeft() && !memberHealth.hasFailed()) {
                builder.addMember(memberHealth.toMember());
            }
        }
        return builder.build();
    }

    public synchronized Membership.MembershipList getMembershipListNoLocal() {
        Membership.MembershipList.Builder builder = Membership.MembershipList.newBuilder();
        for (MemberHealth memberHealth : memberHealths) {
            if (!memberHealth.hasLeft() && !memberHealth.hasFailed()) {
                builder.addMember(memberHealth.toMember());
            }
        }
        return builder.build();
    }

    public synchronized String getLocalId() {
        return localMemberHealth.getId();
    }

    public synchronized Membership.Member getRandomMember() {
        int index = (int) (Math.random() * memberHealths.size());
        return memberHealths.toArray(new MemberHealth[memberHealths.size()])[index].toMember();
    }

    public synchronized Membership.Member getMember(String host, int port) {
        for (MemberHealth memberHealth : memberHealths) {
            if (memberHealth.getHost().equals(host) && memberHealth.getPort() == port)
                return memberHealth.toMember();
        }
        return null;
    }

    /**
     * update is called before sending a heartbeat to a random member. The
     * MemberHealth that represents the local machine gets it's heartbeat incremented and
     * all other MemberHealths are checked for liveness and have their states updated accordingly.
     */
    public synchronized void update() {
        localMemberHealth.setHeartbeat(localMemberHealth.getHeartbeat() + 1);
        long currentTime = System.currentTimeMillis();
        List<MemberHealth> removals = new ArrayList<>();
        for (MemberHealth memberHealth : memberHealths) {
            if (currentTime - memberHealth.getLastSeen() > 5500) {
                removals.add(memberHealth);
            } else if (currentTime - memberHealth.getLastSeen() > 2750) {
                if (!memberHealth.hasFailed() && !memberHealth.hasLeft()) {
                    memberHealth.setHasFailed(true);
                    logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has failed");
                }
            } else {
                if (memberHealth.hasFailed()) {
                    logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has rejoined");
                }
                memberHealth.setHasFailed(false);
            }
        }
        for (MemberHealth memberHealth : removals) {
            memberHealths.remove(memberHealth);
            logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has been removed");
        }
    }

    /**
     * we keep the address and port of the introducer in a properties file which this method reads.
     *
     * @return
     */
    private static String readPropertiesFile() {
        Properties prop = new Properties();

        try (InputStream inputStream = MembershipService.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file 'config.properties' not found in the classpath");
            }
        } catch (Exception e) {
            return "";
        }
        return prop.getProperty("introducer");
    }
}

/**
 * MemberHealth is a mutable and slightly more robust version our Protobuf Member.
 * It holds all the information we need to know about members in our group for
 * failure detection.
 * <p/>
 * MemberHealth is deliberately not a public class so that nothing outside of
 * MembershipService can access or change instances.  This provides safety from
 * bugs where MemberHealth may be accidentally modified elsewhere and cause failure
 * detection to break.
 */
class MemberHealth {
    private final String host;
    private final int port;
    private final long timestamp;
    private long lastSeen;
    private int heartbeat;
    private boolean hasLeft;
    private boolean hasFailed;

    public MemberHealth(Membership.Member member) {
        this(member.getHost(), member.getPort(), member.getTimestamp(), member.getHearbeat());
    }

    public MemberHealth(String host, int port, long timestamp, int heartbeat) {
        this.host = host;
        this.port = port;
        this.timestamp = timestamp;
        lastSeen = System.currentTimeMillis();
        this.heartbeat = heartbeat;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public boolean hasLeft() {
        return hasLeft;
    }

    public void setHasLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    public void setHasFailed(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public boolean matches(Membership.Member member) {
        return host.equals(member.getHost())
                && port == member.getPort()
                && timestamp == member.getTimestamp();
    }

    /**
     * Updates the heartbeat and last seen values of a matching member when needed
     *
     * @param member
     */
    public void merge(Membership.Member member) {
        if (!matches(member)) {
            return;
        }
        if (heartbeat < member.getHearbeat()) {
            heartbeat = member.getHearbeat();
            lastSeen = System.currentTimeMillis();
        }
    }

    /**
     * @return an immutable Member that represents this MemberHealth
     */
    public Membership.Member toMember() {
        return Membership.Member.newBuilder()
                .setHost(host)
                .setPort(port)
                .setTimestamp(timestamp)
                .setHearbeat(heartbeat)
                .build();
    }

    public String getId() {
        return getHost() + ":" + getPort() + ":" + getTimestamp();
    }
}