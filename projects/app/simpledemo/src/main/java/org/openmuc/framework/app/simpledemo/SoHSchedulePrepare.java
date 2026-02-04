package org.openmuc.framework.app.simpledemo;

import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.dataaccess.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.sql.Timestamp;


public class SoHSchedulePrepare {
    private static final Logger logger = LoggerFactory.getLogger(SoHSchedulePrepare.class);

    public static void implement(DataAccessService dataAccessService, List<String> channelIds){
        Path path = Path.of("./conf/custom/sql.cfg");
        try{
            Map<String, String> cfg = ConfigExtract.parseConfigFile(path);
            // logger.info("URL: {}", cfg.get("url"));
            // logger.info("User: {}", cfg.get("user"));
            // logger.info("password: {}", cfg.get("password"));
            // logger.info("Interval: {}", cfg.get("interval"));
            String url = cfg.get("url");
            String user = cfg.get("user");
            String password = cfg.get("password");

            for (String channelId : channelIds) {
                int value = dataAccessService.getChannel(channelId).getLatestRecord().getValue().asInt();
                pushDataToDb(url, password, user, channelId, value);
            }

        }catch(Exception error){
            logger.warn(error.getMessage());
        }

    }
    private static void pushDataToDb(String url, String password, String user, String channelId, int value){

        String sql = "INSERT INTO " + channelId + " (\"time\", flag, \"VALUE\") VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
            ps.setTimestamp(1, Timestamp.from(zdt.toInstant())); // time
            ps.setShort(2, (short) 1);                           // flag
            ps.setInt(3, value);                                 // VALUE
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("---------- Error:  Pushing MODBUS to DB -------------");
        }
    }
    public static int getInterval(){
        int interval = -1;
        Path path = Path.of("./conf/custom/sql.cfg");
        try {
            Map<String, String> cfg = ConfigExtract.parseConfigFile(path);
            interval = Integer.parseInt(cfg.get("interval"));
        } catch(Exception error) {
            logger.warn(error.getMessage());
        }
        return interval;
    }
}
