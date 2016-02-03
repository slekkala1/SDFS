package tempest.commands.interfaces;

/**
 * CommandExecutor extends CommandHandler and provides for the execution of a command that
 * does not provide any response
 * @param <TCommand> type of Command to be executed
 * @param <TRequest> type of the request the Command contains
 */
public interface CommandExecutor<TCommand extends Command<TRequest>, TRequest> extends CommandHandler<TCommand> {
    void execute(TRequest request);
}
