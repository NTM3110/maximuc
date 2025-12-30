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

        String copySql =
                "COPY (" +
                        "  SELECT channelid, value_type, value_double, value_string, value_boolean, updated_at " +
                        "  FROM latest_values " +
                        "  ORDER BY channelid" +
                        ") TO STDOUT WITH (FORMAT csv, HEADER true)";

        try (Connection conn = dataSource.getConnection();
             OutputStream out = resp.getOutputStream()) {

//            PGConnection pgConn = conn.unwrap(PGConnection.class);
//            CopyManager copyManager = pgConn.getCopyAPI();
//
//            copyManager.copyOut(copySql, out); // stream DB -> HTTP response
            out.write("Hello World".getBytes(StandardCharsets.UTF_8));
            out.flush();

        } catch (Exception e) {
            // If we already started writing bytes, reset may fail; still try to return a clean error.
            safeWriteError(resp, e);
            System.err.println("Error at export latest value csv: " + e.getMessage());
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

