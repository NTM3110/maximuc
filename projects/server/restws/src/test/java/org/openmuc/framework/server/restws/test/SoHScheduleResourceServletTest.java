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

import org.openmuc.framework.server.restws.servlets.SoHScheduleResourceServlet;


public class SoHScheduleResourceServletTest {

    SoHScheduleResourceServlet servlet;
    @BeforeEach
    public void setUp() {
        servlet = new SoHScheduleResourceServlet();
    }


    @Test
    public void testGetListReturnsJson() throws Exception {

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/get-list");
        when(req.getQueryString()).thenReturn(null);

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
        System.out.println("testGetListReturnsJson -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("\"data\""));
    }

    @Test
    public void testCreateSchedule() throws Exception {

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/create");
        when(req.getQueryString()).thenReturn("strId=str1&startTime=2025-12-22T16:00:00&current=50");
        when(req.getParameter("stringId")).thenReturn("str1");
        when(req.getParameter("startTime")).thenReturn("2025-12-22T16:00:00");
        when(req.getParameter("current")).thenReturn("50");

        // capture OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) { /* no-op */ }
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doPost(req, resp);

        // servlet may close the stream; baos still contains data
        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testCreateSchedule -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("created successfully"));
    }

    @Test 
    public void testUpdateSchedule() throws Exception {

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/update");
        when(req.getQueryString()).thenReturn("id=8&startTime=2025-12-23T10:00:00");
        when(req.getParameter("id")).thenReturn("8");
        when(req.getParameter("startTime")).thenReturn("2025-12-23T10:00:00");

        // capture OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) { /* no-op */ }
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doPost(req, resp);

        // servlet may close the stream; baos still contains data
        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testUpdateSchedule -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("updated successfully"));
    }

    @Test
    public void testStopSchedule() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/stop");
        when(req.getQueryString()).thenReturn("id=8");
        when(req.getParameter("id")).thenReturn("8");

        // capture OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) { /* no-op */ }
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doPost(req, resp);

        // servlet may close the stream; baos still contains data
        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testStopSchedule -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("stopped successfully"));
    }

    @Test
    public void testRemoveSchedule() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("/delete");
        when(req.getQueryString()).thenReturn("id=8");
        when(req.getParameter("id")).thenReturn("8");  

            // capture OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int b) { baos.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) { /* no-op */ }
        };
        when(resp.getOutputStream()).thenReturn(sos);

        servlet.doPost(req, resp);

        // servlet may close the stream; baos still contains data
        String output = baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        System.out.println("testRemoveSchedule -> " + output);

        assertNotNull(output);
        assertTrue(output.contains("removed successfully"));
    }
}