package tempest.services;

/**
 * Created by swapnalekkala on 11/7/15.
 */
public class FileReplica {

    private int replica1;
    private int replica2;

    public FileReplica(int replica1, int replica2) {
        this.replica1 = replica1;
        this.replica2 = replica2;
    }

    public int getReplica1() {
        return this.replica1;
    }

    public int getReplica2() {
        return this.replica2;
    }

    public void setReplica2(int replica2) {
        this.replica2 = replica2;
    }

    public void setReplica1(int replica1) {
        this.replica1 = replica1;
    }
}
