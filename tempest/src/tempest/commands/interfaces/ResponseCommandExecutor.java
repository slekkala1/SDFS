package tempest.commands.interfaces;

import java.net.Socket;

/**
 * ResponseCommandExecutor extends CommandHandler and provides for the execution of a command that
 * provides a response
 *
 * @param <TCommand>  type of Command to be executed
 * @param <TRequest>  type of request the Command contains
 * @param <TResponse> type of response the execution/Command will return
 */
public interface ResponseCommandExecutor<TCommand extends ResponseCommand<TRequest, TResponse>, TRequest, TResponse> extends CommandHandler<TCommand> {
    TResponse execute(Socket socket, ResponseCommand<TRequest, TResponse> command);
}
