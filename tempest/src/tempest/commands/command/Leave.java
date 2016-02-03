package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Membership;

public class Leave implements ResponseCommand<Membership.Member, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.LEAVE;
    private Membership.Member request;
    private String response;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public Membership.Member getRequest() {
        return request;
    }

    public void setRequest(Membership.Member request) {
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
