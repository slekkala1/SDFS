package tempest.commands.command;

import tempest.commands.interfaces.Command;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Udp;

public class Membership implements Command<tempest.protos.Membership.MembershipList>, Udp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.MEMBERSHIP;
    private tempest.protos.Membership.MembershipList request;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public tempest.protos.Membership.MembershipList getRequest() {
        return request;
    }

    public void setRequest(tempest.protos.Membership.MembershipList request) {
        this.request = request;
    }
}
