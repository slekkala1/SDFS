package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Membership;

public class Introduce implements ResponseCommand<Membership.Member, Membership.MembershipList>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.INTRODUCE;
    private Membership.Member request;
    private Membership.MembershipList response;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public Membership.Member getRequest() {
        return request;
    }

    public void setRequest(Membership.Member request) {
        this.request = request;
    }

    public Membership.MembershipList getResponse() {
        return response;
    }

    public void setResponse(Membership.MembershipList response) {
        this.response = response;
    }

    public Membership.MembershipList add(Membership.MembershipList response1, Membership.MembershipList response2) {
        return null; // not used
    }
}
