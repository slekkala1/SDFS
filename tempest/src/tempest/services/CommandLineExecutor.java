package tempest.services;

import tempest.interfaces.Executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandLineExecutor implements Executor {
    public String[] execute(String command, String options) {
        try {
            List<String> results = new ArrayList<>();
            String commandWithOptions = command + " " + options;
            Process process = Runtime.getRuntime().exec(commandWithOptions);
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine ()) != null) {
                results.add(line);
            }
            return results.toArray(new String[results.size()]);
        } catch (IOException e) {
            return null;
        }
    }
}
