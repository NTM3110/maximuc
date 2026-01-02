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

        try (Connection conn = dataSource.getConnection()) {

           PGConnection pgConn = conn.unwrap(PGConnection.class);
           CopyManager cm = pgConn.getCopyAPI();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long rows = cm.copyOut(copySql, baos);

            byte[] bytes = baos.toByteArray();

            // Debug: if bytes.length == 0, you know DB/COPY produced nothing (or failed earlier)
            // You can temporarily log these:
            System.out.println("copyOut rows=" + rows + " bytes=" + bytes.length);

            resp.setContentLength(bytes.length);
            resp.getOutputStream().write(bytes);
            resp.getOutputStream().flush();

        } catch (Exception e) {
            // Don't swallow it—return the error so you can see it in Postman
        try {
            resp.reset();
            resp.setStatus(500);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().write("Export failed: " + e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception ignored) {}
            e.printStackTrace();
        }
    }

    private static PGConnection unwrapPg(Connection conn) throws Exception {
        if (conn.isWrapperFor(PGConnection.class)) {
            return conn.unwrap(PGConnection.class);
        }
        if (conn instanceof PGConnection) {
            return (PGConnection) conn;
        }
        throw new IllegalStateException("Connection is not a PostgreSQL connection. Actual type: " + conn.getClass());
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

