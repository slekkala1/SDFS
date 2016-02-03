package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;

import java.io.InputStream;

/**
 * Created by swapnalekkala on 10/29/15.
 */
public class PutChunk implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.PUTCHUNK;
    private String request;
    private String response;
    private byte[] byteArray;
    private int replica1;
    private int replica2;
    private String sDFSFileName;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public String getsDFSFileName() {
        return sDFSFileName;
    }

    public void setsDFSFileName(String sDFSFileName) {
        this.sDFSFileName = sDFSFileName;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getReplica1() {
        return replica1;
    }

    public void setReplica1(int replica1) {
        this.replica1 = replica1;
    }

    public int getReplica2() {
        return replica2;
    }

    public void setReplica2(int replica2) {
        this.replica2 = replica2;
    }

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }
}