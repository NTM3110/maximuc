package org.openmuc.framework.server.connectivity.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openmuc.framework.server.connectivity.core.*;
import org.openmuc.framework.server.connectivity.model.VpnConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * REST servlet for VPN connection management.
 * Provides endpoints for CRUD operations on VPN connections.
 */
public class VpnConnectionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VpnConnectionServlet.class);
    private static final Gson gson = new Gson();
    private static final String SWANCTL_CONF_DIR = "/etc/swanctl/conf.d/";
    private static final String SWANCTL_SECRETS_DIR = "/etc/swanctl/secrets.d/";

    private final SwanctlWrapper swanctlWrapper;
    private final ConfigGenerator configGenerator;
    private final ConnectionParser connectionParser;

    public VpnConnectionServlet() {
        this.swanctlWrapper = new SwanctlWrapper();
        this.configGenerator = new ConfigGenerator();
        this.connectionParser = new ConnectionParser(swanctlWrapper);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/vpn/connections - List all connections
                try {
                    List<VpnConnection> connections = connectionParser.getAllConnections();
                    resp.getWriter().write(gson.toJson(connections));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (Exception e) {
                    logger.error("Error getting connections", e);
                    // Return empty array on error instead of failing
                    resp.getWriter().write("[]");
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                // GET /api/vpn/connections/{name}
                String connectionName = pathInfo.substring(1);
                VpnConnection conn = connectionParser.getConnectionByName(connectionName);

                if (conn != null) {
                    resp.getWriter().write(gson.toJson(conn));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Connection not found");
                }
            }
        } catch (Exception e) {
            logger.error("Error processing GET request", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/vpn/connections - Create new connection
                createConnection(req, resp);
            } else {
                // POST /api/vpn/connections/{name}/start or /stop
                handleConnectionAction(pathInfo, resp);
            }
        } catch (Exception e) {
            logger.error("Error processing POST request", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // PUT /api/vpn/connections/{name} - Update connection
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            createConnection(req, resp); // Reuse create logic
        } catch (Exception e) {
            logger.error("Error processing PUT request", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Connection name required");
            return;
        }

        try {
            String connectionName = pathInfo.substring(1);
            File confFile = new File(SWANCTL_CONF_DIR + connectionName + ".conf");

            if (!confFile.exists()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Connection not found");
                return;
            }

            if (confFile.delete()) {
                // Also delete secrets file if exists
                File secretsFile = new File(SWANCTL_SECRETS_DIR + connectionName + ".secrets.conf");
                if (secretsFile.exists()) {
                    secretsFile.delete();
                }

                // Reload configuration
                SwanctlWrapper.CommandResult result = swanctlWrapper.loadAllConnections();
                if (result.isSuccess()) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Failed to reload configuration: " + result.getError());
                }
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete configuration file");
            }
        } catch (Exception e) {
            logger.error("Error processing DELETE request", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void createConnection(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }

        VpnConnection conn = gson.fromJson(buffer.toString(), VpnConnection.class);

        if (conn.getName() == null || conn.getName().isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Connection name is required");
            return;
        }

        String confContent = "";
        String authMethod = conn.getAuthMethod();

        // Generate configuration based on auth method
        if ("ikev2-psk".equals(authMethod)) {
            confContent = configGenerator.generateIKEv2PskConfig(conn);

            // Create secrets file for PSK
            String secretsContent = configGenerator.generateSecretsFile(conn);
            File secretsFile = new File(SWANCTL_SECRETS_DIR + conn.getName() + ".secrets.conf");
            try (FileWriter writer = new FileWriter(secretsFile)) {
                writer.write(secretsContent);
            }
        } else if ("ikev2-cert".equals(authMethod)) {
            confContent = configGenerator.generateIKEv2CertConfig(conn);
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsupported auth_method: " + authMethod);
            return;
        }

        // Write configuration file
        File confFile = new File(SWANCTL_CONF_DIR + conn.getName() + ".conf");
        try (FileWriter writer = new FileWriter(confFile)) {
            writer.write(confContent);
        }

        // Reload configuration if requested
        JsonObject requestData = gson.fromJson(buffer.toString(), JsonObject.class);
        boolean andUpdate = requestData.has("andUpdate") && requestData.get("andUpdate").getAsBoolean();

        if (andUpdate) {
            SwanctlWrapper.CommandResult result = swanctlWrapper.reloadConfiguration();
            if (!result.isSuccess()) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to reload configuration: " + result.getError());
                return;
            }
        }

        JsonObject response = new JsonObject();
        response.addProperty("message", "Connection '" + conn.getName() + "' saved successfully!");
        resp.getWriter().write(gson.toJson(response));
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    private void handleConnectionAction(String pathInfo, HttpServletResponse resp) throws IOException {
        String[] parts = pathInfo.substring(1).split("/");
        if (parts.length < 2) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            return;
        }

        String connectionName = parts[0];
        String action = parts[1];

        SwanctlWrapper.CommandResult result;

        if ("start".equals(action)) {
            result = swanctlWrapper.initiateConnection(connectionName);
        } else if ("stop".equals(action)) {
            result = swanctlWrapper.terminateConnection(connectionName);
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);
            return;
        }

        if (result.isSuccess()) {
            JsonObject response = new JsonObject();
            response.addProperty("message", "Connection " + action + " successful");
            resp.getWriter().write(gson.toJson(response));
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result.getError());
        }
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        resp.setStatus(statusCode);
        resp.getWriter().write(gson.toJson(error));
    }
}
