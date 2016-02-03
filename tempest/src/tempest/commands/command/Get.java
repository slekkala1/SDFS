package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class Get implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.GET;
    private String request;
    private String response;

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

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }
}
