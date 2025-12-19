package org.openmuc.framework.server.restws.servlets.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.server.restws.servlets.LatestValueResourceServlet;
import org.openmuc.framework.lib.rest1.service.LatestValueService;
import org.openmuc.framework.lib.rest1.service.impl.LatestValueServiceImpl;
import java.util.Map;

public class LatestValueResourceServletTest {

    LatestValueResourceServlet servlet;
    LatestValueService latestValueService;

    @BeforeEach
    public void setUp() {
        servlet = new LatestValueResourceServlet();
        latestValueService = new LatestValueServiceImpl();
        servlet.latestValueService = latestValueService; // package-private field
    }

    @Test
    public void testGetDevReturnsJson() throws Exception {
        // keep mocks and injection
        // when(latestValueService.getDevValues()).thenReturn(Map.of("dev_serial_comm_number", "3.0"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/dev");
        when(req.getQueryString()).thenReturn(null);
        // when(resp.isCommitted()).thenReturn(false);

        // capture OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) { /* no-op */ }
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doGet(req, resp);

        // servlet may close the stream; baos still contains data
        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testGetDevReturnsJson -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("\"dev_serial_comm_number\""));
        // verify(latestValueService).getDevValues();
    }

    @Test
    public void testGetSiteNameReturnsJson() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/site-name");
        when(req.getQueryString()).thenReturn(null);
        // when(resp.isCommitted()).thenReturn(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doGet(req, resp);

        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testGetSiteNameReturnsJson -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("\"data\""));
        assertTrue(output.contains("Site1"));
    }

    @Test
    public void testGetStringDetailsReturnsJson() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/string/2");
        when(req.getQueryString()).thenReturn(null);
        // when(resp.isCommitted()).thenReturn(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doGet(req, resp);

        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testGetStringDetailsReturnsJson -> " + output);

        assertNotNull(output);
        // check for string detail fields
        assertTrue(output.contains("stringName") || output.contains("cellBrand") || output.contains("cellModel"));
    }

    @Test
    public void testGetAccountDetailsReturnsJson() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/account/1");
        when(req.getQueryString()).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doGet(req, resp);

        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testGetAccountDetailsReturnsJson -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("username") || output.contains("password"));
    }
}