package org.openmuc.framework.server.connectivity.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.openmuc.framework.server.connectivity.model.ConnectionStatus;
import org.openmuc.framework.server.connectivity.model.VpnConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for strongSwan configuration files and connection status.
 */
public class ConnectionParser {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionParser.class);
    private static final Gson gson = new Gson();
    private static final String SWANCTL_CONF_DIR = "/etc/swanctl/conf.d/";

    private final SwanctlWrapper swanctlWrapper;

    public ConnectionParser(SwanctlWrapper swanctlWrapper) {
        this.swanctlWrapper = swanctlWrapper;
    }

    /**
     * Parse a connection configuration file.
     *
     * @param confFile Configuration file
     * @return VpnConnection object or null if parsing fails
     */
    public VpnConnection parseConnectionFile(File confFile) {
        try {
            String content = new String(Files.readAllBytes(confFile.toPath()));

            VpnConnection conn = new VpnConnection();

            // Parse metadata from first line comment
            Pattern metadataPattern = Pattern.compile("# METADATA: (.*)");
            Matcher metadataMatcher = metadataPattern.matcher(content);
            if (metadataMatcher.find()) {
                try {
                    JsonObject metadata = gson.fromJson(metadataMatcher.group(1), JsonObject.class);
                    if (metadata.has("category")) {
                        conn.setCategory(metadata.get("category").getAsString());
                    }
                    if (metadata.has("auth_method")) {
                        conn.setAuthMethod(metadata.get("auth_method").getAsString());
                    }
                } catch (JsonSyntaxException e) {
                    logger.warn("Failed to parse metadata for file {}", confFile.getName(), e);
                }
            }

            // Parse connection name
            Pattern namePattern = Pattern.compile("connections\\s*\\{\\s*\"([^\"]+)\"");
            Matcher nameMatcher = namePattern.matcher(content);
            if (nameMatcher.find()) {
                conn.setName(nameMatcher.group(1));
            } else {
                // Fallback to filename without extension
                String filename = confFile.getName();
                conn.setName(filename.substring(0, filename.lastIndexOf('.')));
            }

            // Parse local address (server address)
            Pattern localAddrPattern = Pattern.compile("local_addrs\\s*=\\s*([^\\s\\n]+)");
            Matcher localAddrMatcher = localAddrPattern.matcher(content);
            if (localAddrMatcher.find()) {
                String localAddr = localAddrMatcher.group(1);
                conn.setServerAddress(localAddr);
            }

            // Parse remote address
            Pattern remoteAddrPattern = Pattern.compile("remote_addrs\\s*=\\s*([^\\s\\n]+)");
            Matcher remoteAddrMatcher = remoteAddrPattern.matcher(content);
            if (remoteAddrMatcher.find()) {
                conn.setRemoteAddress(remoteAddrMatcher.group(1));
            }

            // Parse local identity
            Pattern localIdPattern = Pattern.compile("local\\s*\\{[^}]*id\\s*=\\s*\"?([^\"\\n]+)\"?", Pattern.DOTALL);
            Matcher localIdMatcher = localIdPattern.matcher(content);
            if (localIdMatcher.find()) {
                conn.setLocalIdentity(localIdMatcher.group(1).trim());
            }

            // Parse remote identity
            Pattern remoteIdPattern = Pattern.compile("remote\\s*\\{[^}]*id\\s*=\\s*\"?([^\"\\n]+)\"?", Pattern.DOTALL);
            Matcher remoteIdMatcher = remoteIdPattern.matcher(content);
            if (remoteIdMatcher.find()) {
                conn.setPeerIdentity(remoteIdMatcher.group(1).trim());
            }

            // Parse traffic selectors
            Pattern localTsPattern = Pattern.compile("local_ts\\s*=\\s*([^\\s\\n]+)");
            Matcher localTsMatcher = localTsPattern.matcher(content);
            if (localTsMatcher.find()) {
                conn.setLocalTrafficSelector(localTsMatcher.group(1));
            }

            Pattern remoteTsPattern = Pattern.compile("remote_ts\\s*=\\s*([^\\s\\n]+)");
            Matcher remoteTsMatcher = remoteTsPattern.matcher(content);
            if (remoteTsMatcher.find()) {
                conn.setRemoteTrafficSelector(remoteTsMatcher.group(1));
            }

            conn.setStatus(ConnectionStatus.IDLE); // Default status, will be updated later

            return conn;

        } catch (IOException e) {
            logger.error("Failed to read configuration file {}", confFile.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Get all VPN connections with their current status.
     *
     * @return List of VpnConnection objects
     */
    public List<VpnConnection> getAllConnections() {
        List<VpnConnection> connections = new ArrayList<>();

        // Get active SAs status
        Map<String, String> activeSAs = swanctlWrapper.listActiveSAs();

        // Parse configuration files
        File confDir = new File(SWANCTL_CONF_DIR);
        if (!confDir.exists() || !confDir.isDirectory()) {
            logger.warn("Configuration directory does not exist: {}", SWANCTL_CONF_DIR);
            return connections;
        }

        File[] confFiles = confDir.listFiles((dir, name) -> name.endsWith(".conf"));
        if (confFiles == null) {
            logger.warn("No configuration files found in {}", SWANCTL_CONF_DIR);
            return connections;
        }

        for (File confFile : confFiles) {
            VpnConnection conn = parseConnectionFile(confFile);
            if (conn != null) {
                // Update status from active SAs
                String saStatus = activeSAs.get(conn.getName());
                if (saStatus != null) {
                    if ("ESTABLISHED".equals(saStatus)) {
                        conn.setStatus(ConnectionStatus.ESTABLISHED);
                    } else if ("CONNECTING".equals(saStatus)) {
                        conn.setStatus(ConnectionStatus.CONNECTING);
                    }
                } else {
                    conn.setStatus(ConnectionStatus.IDLE);
                }

                connections.add(conn);
            }
        }

        return connections;
    }

    /**
     * Get a specific connection by name.
     *
     * @param name Connection name
     * @return VpnConnection or null if not found
     */
    public VpnConnection getConnectionByName(String name) {
        File confFile = new File(SWANCTL_CONF_DIR + name + ".conf");
        if (!confFile.exists()) {
            return null;
        }

        VpnConnection conn = parseConnectionFile(confFile);
        if (conn != null) {
            // Update with current status
            Map<String, String> activeSAs = swanctlWrapper.listActiveSAs();
            String saStatus = activeSAs.get(name);
            if (saStatus != null) {
                conn.setStatus(
                        "ESTABLISHED".equals(saStatus) ? ConnectionStatus.ESTABLISHED : ConnectionStatus.CONNECTING);
            }
        }

        return conn;
    }
}
