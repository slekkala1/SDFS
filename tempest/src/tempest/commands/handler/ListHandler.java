package tempest.commands.handler;

import tempest.commands.command.Grep;
import tempest.commands.command.List;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.services.Partitioner;

import java.net.Inet4Address;
import java.net.Socket;

/**
 * Created by swapnalekkala on 11/6/15.
 */
public class ListHandler implements ResponseCommandExecutor<List, String, String> {
    private Partitioner partitioner;

    public ListHandler(Partitioner partitioner) {
        this.partitioner = partitioner;
    }

    public boolean canHandle(Command.Message.Type type) {
        return List.type == type;
    }

    public Command.Message serialize(List command) {
        Command.List.Builder listBuilder = Command.List.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            listBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.LIST)
                .setList(listBuilder)
                .build();
        return message;
    }

    public List deserialize(Command.Message message) {
        List list = new List();
        list.setRequest(message.getList().getRequest());
        if (message.getList().hasResponse())
            list.setResponse(message.getList().getResponse());
        return list;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {

        for (String sDFSFileName : this.partitioner.getsDFSFileNamesAtTheVM().values()) {
            if (sDFSFileName.contains(command.getRequest())) return this.partitioner.getLocalHostName();
        }
        return "";
    }
}
