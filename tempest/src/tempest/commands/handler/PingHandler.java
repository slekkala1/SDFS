package tempest.commands.handler;

import tempest.commands.command.Ping;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;

import java.net.Socket;
import java.util.Objects;

public class PingHandler implements ResponseCommandExecutor<Ping, Object, String> {
    public boolean canHandle(Command.Message.Type type) {
        return type == Ping.type;
    }

    public Command.Message serialize(Ping command) {
        Command.Ping.Builder pingBuilder = Command.Ping.newBuilder();
        if (command.getResponse() != null)
            pingBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.PING)
                .setPing(pingBuilder)
                .build();
        return message;
    }

    public Ping deserialize(Command.Message message) {
        Ping ping = new Ping();
        if (message.hasPing() && message.getPing().hasResponse())
            ping.setResponse(message.getPing().getResponse());
        return ping;
    }

    public String execute(Socket socket, ResponseCommand<Object, String> command) {
        return "Hello";
    }
}
