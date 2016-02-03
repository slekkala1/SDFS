package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Command;

/**
 * Created by swapnalekkala on 11/1/15.
 */
public class GetChunk implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = Command.Message.Type.GETCHUNK;
    private String request;
    private String response;
    private int bytesSize;
    private byte[] byteArray;

    public tempest.protos.Command.Message.Type getType() {
        return type;
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

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public int getBytesSize() {
        return bytesSize;
    }

    public void setBytesSize(int bytesSize) {
        this.bytesSize = bytesSize;
    }

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }
}