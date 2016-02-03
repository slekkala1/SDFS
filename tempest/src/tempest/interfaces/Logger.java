package tempest.interfaces;

/**
 * Logger implementations provide simple logging and searching of logs via grep
 * functionality.
 */
public interface Logger {
    String INFO = "info";
    String WARNING = "warning";
    String SEVERE = "severe";

    void logLine(String level, String message);
    String grep(String options);
    String getLogFile();
    String getGrepFile();
}
