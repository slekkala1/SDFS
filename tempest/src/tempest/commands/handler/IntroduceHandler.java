package tempest.commands.handler;

import tempest.commands.interfaces.ResponseCommand;
import tempest.services.MembershipService;
import tempest.commands.command.Introduce;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.protos.Membership;

import java.net.Socket;

public class IntroduceHandler implements ResponseCommandExecutor<Introduce, Membership.Member, Membership.MembershipList> {
    private final MembershipService membershipService;
    private final Logger logger;

    public IntroduceHandler(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.logger = logger;
    }

    public boolean canHandle(Command.Message.Type type) {
        return Introduce.type == type;
    }

    public Command.Message serialize(Introduce command) {
        Command.Introduce.Builder introduceBuilder = Command.Introduce.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            introduceBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.INTRODUCE)
                .setIntroduce(introduceBuilder)
                .build();
        return message;
    }

    public Introduce deserialize(Command.Message message) {
        Introduce introduce = new Introduce();
        introduce.setRequest(message.getIntroduce().getRequest());
        if (message.getIntroduce().hasResponse())
            introduce.setResponse(message.getIntroduce().getResponse());
        return introduce;
    }

    public Membership.MembershipList execute(Socket socket, ResponseCommand<Membership.Member, Membership.MembershipList> command) {
        membershipService.addMember(command.getRequest());
        return membershipService.getMembershipList();
    }
}
