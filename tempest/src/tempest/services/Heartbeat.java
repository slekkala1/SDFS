package tempest.services;

import java.util.concurrent.*;

public class Heartbeat implements Runnable {
    private final ScheduledExecutorService scheduler;
    private final Client client;
    private MembershipService membershipService;

    public Heartbeat(Client client, MembershipService membershipService) {
        scheduler =  Executors.newScheduledThreadPool(1);
        this.client = client;
        this.membershipService = membershipService;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this, 0, 250, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void run() {
        if (membershipService.getMembershipList().getMemberCount() < 2)
            return;
        membershipService.update();
        client.membership(membershipService.getRandomMember(), membershipService.getMembershipList());
    }
}
