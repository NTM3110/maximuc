package org.openmuc.framework.lib.rest1.service.impl;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.Statement;

public final class CsvExportUtil {

    private CsvExportUtil() {}

    public static void exportLatestValuesCsv(HttpServletResponse resp, DataSource dataSource, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            fileName = "latest_values.csv";
        } else if (!fileName.toLowerCase().endsWith(".csv")) {
            fileName = fileName + ".csv";
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + sanitizeFileName(fileName) + "\"");
        // Optional: prevent caching
        resp.setHeader("Cache-Control", "no-store");

        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT channelid, value_type, value_double, value_string, value_boolean, updated_at " +
                "FROM latest_values ORDER BY channelid");
            OutputStream out = resp.getOutputStream()) {

            out.write("channelid,value_type,value_double,value_string,value_boolean,updated_at\n"
                    .getBytes(StandardCharsets.UTF_8));

            while (rs.next()) {
                out.write((
                    rs.getString(1) + "," +
                    rs.getString(2) + "," +
                    rs.getObject(3) + "," +
                    rs.getString(4) + "," +
                    rs.getObject(5) + "," +
                    rs.getTimestamp(6) + "\n"
                ).getBytes(StandardCharsets.UTF_8));
            }

            out.flush();
        }catch (Exception e) {
            // If we already started writing bytes, reset may fail; still try to return a clean error.
            safeWriteError(resp, e);
            System.out.println("Error at export latest value csv: " + e.getMessage());
        }
    }

    public static void importLatestValuesCsv(DataSource dataSource, ByteArrayOutputStream csvData, Charset charset) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            CopyManager copyManager = conn.unwrap(PGConnection.class).getCopyAPI();
            String copyCommand = "COPY latest_values (channelid, value_type, value_double, value_string, value_boolean, updated_at) FROM STDIN WITH (FORMAT csv, HEADER true, DELIMITER ',', NULL '')";
            copyManager.copyIn(copyCommand, new java.io.ByteArrayInputStream(csvData.toByteArray()));
        }
    }

    private static String sanitizeFileName(String name) {
        // keep it simple: remove path separators and quotes
        return name.replace("\\", "_")
                .replace("/", "_")
                .replace("\"", "_")
                .replace("\n", "_")
                .replace("\r", "_");
    }

    private static void safeWriteError(HttpServletResponse resp, Exception e) {
        try {
            if (!resp.isCommitted()) {
                resp.reset();
                resp.setStatus(500);
                resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                resp.setContentType("text/plain; charset=UTF-8");
                resp.getWriter().write("Export failed: " + e.getMessage());
            }
        } catch (Exception ignored) {
            // nothing else we can do
        }
    }
}

