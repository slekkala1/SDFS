package tempest.interfaces;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Log wrapper provides a simple wrapper around java.util.logging.Logger.
 * It exists purely to facilitate testign since we're not using a mock
 * framework at this point.
 */
public interface LogWrapper {
    void addFileHandler(String file) throws IOException;
    void logp(Level level, String sourceClass, String sourceMethod, String msg);
}
