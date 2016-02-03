package tempest.commands.interfaces;

/**
 * Extends Command and allows a response to be returned from the Server back to the
 * Client.
 * @param <TRequest> type of request
 * @param <TResponse> type of response
 */
public interface ResponseCommand<TRequest, TResponse> extends Command<TRequest> {
    TResponse getResponse();
    void setResponse(TResponse response);

    /**
     * add allows the response from two Commands to be merged together and returned
     * as a single response.  This was originally added to allow for the responses
     * from multicast messages such as Grep to be aggregated. It may make sense to
     * create something like a MulticastResponseCommand in the future that this method
     * moves to.
     * @param response1
     * @param response2
     * @return the aggregate response from response1 and response2
     */
    TResponse add(TResponse response1, TResponse response2);
}
