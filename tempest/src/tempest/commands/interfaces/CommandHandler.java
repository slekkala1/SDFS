package tempest.commands.interfaces;

/**
 * CommandHandler is used by Client and Server to serialize and deserialize Commands
 * @param <TCommand> Type of the command that is handled
 */
public interface CommandHandler<TCommand extends Command> {
    /**
     * canHandle returns true if this handler can handle the command type
     * @param type
     * @return
     */
    boolean canHandle(tempest.protos.Command.Message.Type type);
    tempest.protos.Command.Message serialize(TCommand command);
    TCommand deserialize(tempest.protos.Command.Message message);
}

