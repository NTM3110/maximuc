package org.openmuc.framework.server.restws.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.DeviceConfig;
import org.openmuc.framework.config.DriverConfig;
import org.openmuc.framework.config.IdCollisionException;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.lib.rest1.Const;
import org.openmuc.framework.lib.rest1.FromJson;
import org.openmuc.framework.lib.rest1.exceptions.MissingJsonObjectException;
import org.openmuc.framework.lib.rest1.exceptions.RestConfigIsNotCorrectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BatteryStringResourceServlet extends GenericServlet {

    private ConfigService configService;
    private RootConfig rootConfig;
    private static final Logger logger = LoggerFactory.getLogger(BatteryStringResourceServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(APPLICATION_JSON);

        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        if (pathAndQueryString == null) {
            return;
        }

        // same pattern as DeviceResourceServlet_v2
        setConfigAccess();

        // 1) parse the SMALL request from FE
        FromJson smallJson = ServletLib.getFromJson(request, logger, response);
        if (smallJson == null) {
            return;
        }
        JsonObject req = smallJson.getJsonObject();

        int s = req.get("stringIndex").getAsInt();
        int cells = req.get("cellQty").getAsInt();
        JsonObject portConfig = req.getAsJsonObject("portConfig");

        // validate basic constraints
        if (cells <= 0 || cells > 400) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger,
                    "cellQty out of allowed range");
            return;
        }

        String modbusId = "str" + s + "_modbus";
        String virtualId = "str" + s + "_virtual";

        // 2) build the BIG payloads (same structure FE builds today)
        JsonObject modbusPayload = buildModbusPayload(s, cells, portConfig);
        JsonObject virtualPayload = buildVirtualPayload(s, cells);

        // 3) apply them using the same logic as DeviceResourceServlet_v2 does in POST
        // (driverConfig.addDevice + FromJson.setDeviceConfigV2 + write config)
        boolean ok1 = createDeviceFromPayload(modbusId, modbusPayload, response);
        if (!ok1) return;

        boolean ok2 = createDeviceFromPayload(virtualId, virtualPayload, response);
        if (!ok2) return;

        // 4) respond success
        JsonObject out = new JsonObject();
        out.addProperty("status", "ok");
        out.addProperty("stringIndex", s);
        sendJson(out, response);
    }

    private void setConfigAccess() {
        this.configService = handleConfigService(null);
        this.rootConfig = handleRootConfig(null);
    }

    // This is basically DeviceResourceServlet_v2.setAndWriteHttpPostDeviceConfig,
    // but taking JsonObject instead of reading from HTTP.
    private synchronized boolean createDeviceFromPayload(String deviceId, JsonObject payload, HttpServletResponse response) {
        try {
            FromJson json = new FromJson(payload.toString());

            DeviceConfig existing = rootConfig.getDevice(deviceId);
            String driverId = payload.get(Const.DRIVER).getAsString();

            DriverConfig driverConfig = rootConfig.getDriver(driverId);
            if (driverConfig == null) {
                ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                        "Driver does not exists: ", driverId);
                return false;
            }
            if (existing != null) {
                ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger,
                        "Device already exists: ", deviceId);
                return false;
            }

            try {
                DeviceConfig deviceConfig = driverConfig.addDevice(deviceId);
                json.setDeviceConfigV2(deviceConfig, deviceId);
            } catch (IdCollisionException ignored) {
            }

            configService.setConfig(rootConfig);
            configService.writeConfigToFile();
            return true;

        } catch (JsonSyntaxException | RestConfigIsNotCorrectException | MissingJsonObjectException e) {
            ServletLib.sendHTTPErrorAndLogDebug(response, HttpServletResponse.SC_NOT_ACCEPTABLE, logger, e.getMessage());
            return false;
        } catch (Exception e) {
            ServletLib.sendHTTPErrorAndLogErr(response, HttpServletResponse.SC_CONFLICT, logger, e.getMessage());
            return false;
        }
    }

    // --- Builders (port your TS builders here) ---

    private JsonObject buildModbusPayload(int s, int cells, JsonObject portConfig) {
        JsonObject payload = new JsonObject();
        payload.addProperty("driver", "modbus");

        JsonObject configs = new JsonObject();
        configs.addProperty("id", "str" + s + "_modbus");
        configs.addProperty("description", "String " + s + " Modbus RTU");
        configs.addProperty("deviceAddress", portConfig.get("port").getAsString());

        // This should match your TS buildModbusSettings result format
        configs.addProperty("settings", buildModbusSettings(portConfig));
        configs.addProperty("samplingTimeout", 12000);
        configs.addProperty("connectRetryInterval", 1000);
        configs.addProperty("disabled", false);

        payload.add("configs", configs);

        JsonArray channels = new JsonArray();
        // TODO: port loop from buildModbusPayload() in TS:
        // for c=1..cells add channel configs exactly like FE does
        for(int i = 1; i <= cells; i++){

        }
        payload.add("channels", channels);

        return payload;
    }

    private JsonObject buildVirtualPayload(int s, int cells) {
        JsonObject payload = new JsonObject();
        payload.addProperty("driver", "virtual");

        JsonObject configs = new JsonObject();
        configs.addProperty("id", "str" + s + "_virtual");
        configs.addProperty("description", "String " + s + " calculated channels");
        configs.addProperty("disabled", false);

        payload.add("configs", configs);

        JsonArray channels = new JsonArray();
        // TODO: port buildVirtualPayload() loop from TS
        payload.add("channels", channels);

        return payload;
    }

    private String buildModbusSettings(JsonObject portConfig) {
        // Build same format as FE used (your TS returns a string like RTU:SERIAL_ENCODING_RTU:... )
        // Example placeholder:
        int baudRate = portConfig.get("baudRate").getAsInt();
        int dataBits = portConfig.get("dataBits").getAsInt();
        String parity = portConfig.get("parity").getAsString();
        String stopBits = portConfig.get("stopBits").getAsString();
        return "RTU:SERIAL_ENCODING_RTU:" + baudRate + ":DATABITS_" + dataBits +
                ":PARITY_" + parity + ":STOPBITS_" + stopBits + ":ECHO_FALSE:FLOWCONTROL_NONE:FLOWCONTROL_NONE";
    }
}
