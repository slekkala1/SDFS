package tempest.interfaces;

import tempest.commands.Response;

import java.util.concurrent.Callable;

/**
 * ResponseClientCommandExecutor is used by the Client to serialize and send a Command to the Server,
 * listen for a response and collect basic data about the response.
 */
public interface ClientResponseCommandExecutor<TResponse> extends Callable<Response<TResponse>> {
    Response<TResponse> execute();
}
