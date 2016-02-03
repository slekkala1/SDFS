package tempest.commands.handler;

import tempest.commands.interfaces.ResponseCommand;
import tempest.services.MembershipService;
import tempest.commands.command.Leave;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;
import tempest.protos.Membership;

import java.net.Socket;

public class LeaveHandler implements ResponseCommandExecutor<Leave, Membership.Member, String> {
    private final MembershipService membershipService;

    public LeaveHandler(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    public boolean canHandle(Command.Message.Type type) {
        return Leave.type == type;
    }

    public Command.Message serialize(Leave command) {
        Command.Leave.Builder leaveBuilder = Command.Leave.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            leaveBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Leave.type)
                .setLeave(leaveBuilder)
                .build();
        return message;
    }

    public Leave deserialize(Command.Message message) {
        Leave leave = new Leave();
        leave.setRequest(message.getLeave().getRequest());
        if (message.getIntroduce().hasResponse())
            leave.setResponse(message.getLeave().getResponse());
        return leave;
    }

    public String execute(Socket socket, ResponseCommand<Membership.Member, String> command) {
        membershipService.memberLeft(command.getRequest());
        return "Bye";
    }
}
