package tempest.interfaces;

import java.util.concurrent.Callable;

/**
 * ClientCommandExecutor is used by the Client to serialize and send Commands to the Server
 */
public interface ClientCommandExecutor extends Callable {
    void execute();
}
