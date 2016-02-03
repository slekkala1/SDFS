package tempest.commands.handler;

import tempest.commands.command.Grep;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;

import java.net.Socket;

public class GrepHandler implements ResponseCommandExecutor<Grep, String, String> {
    private final Logger logger;

    public GrepHandler(Logger logger) {

        this.logger = logger;
    }

    public boolean canHandle(Command.Message.Type type) {
        return Grep.type == type;
    }

    public Command.Message serialize(Grep command) {
        Command.Grep.Builder grepBuilder = Command.Grep.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            grepBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GREP)
                .setGrep(grepBuilder)
                .build();
        return message;
    }

    public Grep deserialize(Command.Message message) {
        Grep grep = new Grep();
        grep.setRequest(message.getGrep().getRequest());
        if (message.getGrep().hasResponse())
            grep.setResponse(message.getGrep().getResponse());
        return grep;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {
        return logger.grep(command.getRequest());
    }
}
