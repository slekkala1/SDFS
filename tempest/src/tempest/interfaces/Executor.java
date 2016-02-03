package tempest.interfaces;

/**
 * Executors provide a way to execute a terminal/shell command and get
 * the results back.
 */
public interface Executor {
    String[] execute(String command, String options);
}
