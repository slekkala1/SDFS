package tempest.commands.interfaces;

/**
 * Command represents a message that is sent via the Client to a Server
 * Commands are created in tandem with a CommandHandler
 * @param <TRequest> The type of the request that will be sent from the command
 */
public interface Command<TRequest> {
    /**
     * getType returns a type that is used to identify the command
     * by the CommandHandler
     * @return
     */
    tempest.protos.Command.Message.Type getType();
    TRequest getRequest();
    void setRequest(TRequest response);
}
