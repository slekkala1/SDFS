package tempest.commands.handler;

import tempest.services.MembershipService;
import tempest.commands.command.Membership;
import tempest.commands.interfaces.CommandExecutor;
import tempest.protos.Command;

public class MembershipHandler implements CommandExecutor<Membership, tempest.protos.Membership.MembershipList> {
    private MembershipService membershipService;

    public MembershipHandler(MembershipService membershipService) {

        this.membershipService = membershipService;
    }

    public boolean canHandle(Command.Message.Type type) {
        return type == Membership.type;
    }

    public Command.Message serialize(Membership command) {
        Command.Membership.Builder membershipBuilder = Command.Membership.newBuilder();
        membershipBuilder.setRequest(command.getRequest());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.MEMBERSHIP)
                .setMembership(membershipBuilder)
                .build();
        return message;
    }

    public Membership deserialize(Command.Message message) {
        Membership membership = new Membership();
        if (message.hasMembership() && message.getMembership().hasRequest())
            membership.setRequest(message.getMembership().getRequest());
        return membership;
    }

    public void execute(tempest.protos.Membership.MembershipList request) {
        membershipService.merge(request);
    }
}
