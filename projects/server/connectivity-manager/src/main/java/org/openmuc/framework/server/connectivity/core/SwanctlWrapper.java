package org.openmuc.framework.server.connectivity.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class for executing swanctl commands.
 * Provides methods to interact with strongSwan via command line.
 */
public class SwanctlWrapper {

    private static final Logger logger = LoggerFactory.getLogger(SwanctlWrapper.class);
    private static final String SWANCTL_CMD = "swanctl";
    private static final String SUDO_CMD = "sudo";

    /**
     * Execute a command and return the result.
     *
     * @param command Command to execute
     * @return CommandResult containing success status and output/error
     */
    public CommandResult executeCommand(String command) {
        logger.debug("Executing command: {}", command);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", command);

        try {
            Process process = processBuilder.start();

            // Read stdout
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Read stderr
            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.debug("Command executed successfully");
                return new CommandResult(true, output.toString().trim(), null);
            } else {
                logger.error("Command failed with exit code {}: {}", exitCode, error.toString());
                return new CommandResult(false, null, error.toString().trim());
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error executing command", e);
            return new CommandResult(false, null, e.getMessage());
        }
    }

    /**
     * Load all connections from configuration files.
     *
     * @return CommandResult with success status
     */
    public CommandResult loadAllConnections() {
        String command = SUDO_CMD + " " + SWANCTL_CMD + " --load-all";
        return executeCommand(command);
    }

    /**
     * Load secrets (PSK, private keys) from configuration.
     *
     * @return CommandResult with success status
     */
    public CommandResult loadSecrets() {
        String command = SUDO_CMD + " " + SWANCTL_CMD + " --load-creds";
        return executeCommand(command);
    }

    /**
     * Reload both connections and secrets.
     *
     * @return CommandResult with success status
     */
    public CommandResult reloadConfiguration() {
        CommandResult secretsResult = loadSecrets();
        if (!secretsResult.isSuccess()) {
            return secretsResult;
        }
        return loadAllConnections();
    }

    /**
     * List all active Security Associations (SAs).
     *
     * @return Map of connection name to status (ESTABLISHED, CONNECTING, etc.)
     */
    public Map<String, String> listActiveSAs() {
        String command = SUDO_CMD + " " + SWANCTL_CMD + " --list-sas";
        CommandResult result = executeCommand(command);

        Map<String, String> activeSAs = new HashMap<>();

        if (result.isSuccess() && result.getOutput() != null) {
            String[] lines = result.getOutput().split("\n");
            for (String line : lines) {
                // Parse lines like: "connection-name: IKE_SA established"
                if (line.contains(":") && (line.contains("ESTABLISHED") || line.contains("CONNECTING"))) {
                    String[] parts = line.split(":");
                    if (parts.length >= 1) {
                        String name = parts[0].trim();
                        String status = line.contains("ESTABLISHED") ? "ESTABLISHED" : "CONNECTING";
                        activeSAs.put(name, status);
                    }
                }
            }
        }

        return activeSAs;
    }

    /**
     * Initiate a connection.
     *
     * @param connectionName Name of the connection to initiate
     * @return CommandResult with success status
     */
    public CommandResult initiateConnection(String connectionName) {
        String childName = connectionName + "-child";
        String command = SUDO_CMD + " " + SWANCTL_CMD + " --initiate --child " + childName;
        return executeCommand(command);
    }

    /**
     * Terminate a connection.
     *
     * @param connectionName Name of the connection to terminate
     * @return CommandResult with success status
     */
    public CommandResult terminateConnection(String connectionName) {
        String command = SUDO_CMD + " " + SWANCTL_CMD + " --terminate --ike " + connectionName;
        return executeCommand(command);
    }

    /**
     * Get version information of swanctl.
     *
     * @return Version string or null if command failed
     */
    public String getVersion() {
        String command = SWANCTL_CMD + " --version";
        CommandResult result = executeCommand(command);
        return result.isSuccess() ? result.getOutput() : null;
    }

    /**
     * Result of a command execution.
     */
    public static class CommandResult {
        private final boolean success;
        private final String output;
        private final String error;

        public CommandResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "CommandResult{" +
                    "success=" + success +
                    ", output='" + output + '\'' +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}
