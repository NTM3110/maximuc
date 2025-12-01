package org.openmuc.framework.server.connectivity.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openmuc.framework.server.connectivity.model.VpnConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates strongSwan configuration files for VPN connections.
 * Based on the Python implementation in strongswan_api.py
 */
public class ConfigGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigGenerator.class);
    private static final Gson gson = new Gson();

    /**
     * Generate swanctl configuration for Site-to-Site IKEv2 with PSK
     * authentication.
     *
     * @param conn VpnConnection object
     * @return Configuration file content as String
     */
    public String generateIKEv2PskConfig(VpnConnection conn) {
        if (conn.getName() == null || conn.getName().isEmpty()) {
            logger.error("Connection name is required");
            return "";
        }

        StringBuilder content = new StringBuilder();

        // Add metadata as JSON comment
        JsonObject metadata = new JsonObject();
        metadata.addProperty("category", conn.getCategory());
        metadata.addProperty("auth_method", conn.getAuthMethod());
        content.append("# METADATA: ").append(gson.toJson(metadata)).append("\n\n");

        // Start connections block
        content.append("connections {\n");
        content.append("  \"").append(conn.getName()).append("\" {\n");

        // Local address
        content.append("    local_addrs = %any\n");

        // Remote address
        if (conn.getRemoteAddress() != null && !conn.getRemoteAddress().isEmpty()) {
            content.append("    remote_addrs = ").append(conn.getRemoteAddress()).append("\n");
        }

        // Local authentication
        content.append("    local {\n");
        content.append("      auth = psk\n");
        if (conn.getLocalIdentity() != null && !conn.getLocalIdentity().isEmpty()) {
            content.append("      id = ").append(conn.getLocalIdentity()).append("\n");
        }
        content.append("    }\n");

        // Remote authentication
        content.append("    remote {\n");
        content.append("      auth = psk\n");
        if (conn.getRemoteIdentity() != null && !conn.getRemoteIdentity().isEmpty()) {
            content.append("      id = ").append(conn.getRemoteIdentity()).append("\n");
        }
        content.append("    }\n");

        // Children (IPsec SA)
        content.append("    children {\n");
        content.append("      \"").append(conn.getName()).append("-child\" {\n");
        content.append("        local_ts = ")
                .append(conn.getLocalTrafficSelector() != null ? conn.getLocalTrafficSelector() : "0.0.0.0/0")
                .append("\n");
        content.append("        remote_ts = ")
                .append(conn.getRemoteTrafficSelector() != null ? conn.getRemoteTrafficSelector() : "0.0.0.0/0")
                .append("\n");
        content.append("        start_action = trap\n");
        content.append("      }\n");
        content.append("    }\n");

        // Connection settings
        content.append("    version = 0\n"); // Any IKE version
        content.append("    mobike = no\n");
        content.append("    auto = add\n");

        content.append("  }\n");
        content.append("}\n");

        return content.toString();
    }

    /**
     * Generate swanctl configuration for Site-to-Site IKEv2 with Certificate
     * authentication.
     *
     * @param conn VpnConnection object
     * @return Configuration file content as String
     */
    public String generateIKEv2CertConfig(VpnConnection conn) {
        if (conn.getName() == null || conn.getName().isEmpty()) {
            logger.error("Connection name is required");
            return "";
        }

        StringBuilder content = new StringBuilder();

        // Add metadata
        JsonObject metadata = new JsonObject();
        metadata.addProperty("category", conn.getCategory());
        metadata.addProperty("auth_method", conn.getAuthMethod());
        content.append("# METADATA: ").append(gson.toJson(metadata)).append("\n\n");

        content.append("connections {\n");
        content.append("  \"").append(conn.getName()).append("\" {\n");

        // IKE version
        String ikeVersion = conn.getIkeVersion() != null ? conn.getIkeVersion() : "any";
        if ("ikev1".equals(ikeVersion)) {
            content.append("    version = 1\n");
        } else if ("ikev2".equals(ikeVersion)) {
            content.append("    version = 2\n");
        } else {
            content.append("    version = 0\n"); // Any version
        }

        // Auto start
        if (conn.isActiveInitiator()) {
            content.append("    auto = start\n");
        } else {
            content.append("    auto = add\n");
        }

        // Addresses
        if (conn.getServerAddress() != null && !conn.getServerAddress().isEmpty()) {
            content.append("    local_addrs = ").append(conn.getServerAddress()).append("\n");
        }
        if (conn.getRemoteAddress() != null && !conn.getRemoteAddress().isEmpty()) {
            content.append("    remote_addrs = ").append(conn.getRemoteAddress()).append("\n");
        }

        // Local authentication with certificate
        content.append("    local {\n");
        content.append("      auth = pubkey\n");
        if (conn.getServerCertificateName() != null && !conn.getServerCertificateName().isEmpty()) {
            content.append("      certs = ").append(conn.getServerCertificateName()).append("\n");
        }
        if (conn.getLocalIdentity() != null && !conn.getLocalIdentity().isEmpty()) {
            content.append("      id = \"").append(conn.getLocalIdentity()).append("\"\n");
        }
        content.append("    }\n");

        // Remote authentication
        content.append("    remote {\n");
        content.append("      auth = pubkey\n");
        if (!conn.isAutoCASelect() && conn.getCaCertificateName() != null && !conn.getCaCertificateName().isEmpty()) {
            content.append("      cacerts = ").append(conn.getCaCertificateName()).append("\n");
        }
        if (!conn.isUseServerValue() && conn.getPeerIdentity() != null && !conn.getPeerIdentity().isEmpty()) {
            content.append("      id = \"").append(conn.getPeerIdentity()).append("\"\n");
        }
        content.append("    }\n");

        // Children
        content.append("    children {\n");
        content.append("      \"").append(conn.getName()).append("-child\" {\n");
        content.append("        local_ts = ")
                .append(conn.getLocalTrafficSelector() != null ? conn.getLocalTrafficSelector() : "0.0.0.0/0")
                .append("\n");
        content.append("        remote_ts = ")
                .append(conn.getRemoteTrafficSelector() != null ? conn.getRemoteTrafficSelector() : "0.0.0.0/0")
                .append("\n");

        String startAction = conn.getStartAction() != null ? conn.getStartAction() : "none";
        content.append("        start_action = ").append(startAction).append("\n");
        content.append("      }\n");
        content.append("    }\n");

        content.append("  }\n");
        content.append("}\n");

        return content.toString();
    }

    /**
     * Generate secrets file for PSK authentication.
     *
     * @param conn VpnConnection with PSK data
     * @return Secrets file content as String
     */
    public String generateSecretsFile(VpnConnection conn) {
        if (conn.getName() == null || conn.getPreSharedKey() == null) {
            logger.error("Connection name and pre-shared key are required");
            return "";
        }

        StringBuilder content = new StringBuilder();
        content.append("ike-psk-").append(conn.getName()).append(" {\n");

        if (conn.getLocalIdentity() != null && !conn.getLocalIdentity().isEmpty()) {
            content.append("    id = ").append(conn.getLocalIdentity()).append("\n");
        }

        content.append("    secret = \"").append(conn.getPreSharedKey()).append("\"\n");
        content.append("}\n");

        return content.toString();
    }
}
