package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Udp;

public class Ping implements ResponseCommand<Object, String>, Udp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.PING;
    private Object request;
    private String response;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }
}
