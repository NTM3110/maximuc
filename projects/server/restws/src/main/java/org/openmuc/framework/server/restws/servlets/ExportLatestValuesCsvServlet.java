package org.openmuc.framework.server.restws.servlets;

import org.openmuc.framework.lib.rest1.service.impl.CsvExportUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

public class ExportLatestValuesCsvServlet extends GenericServlet{
    private final DataSource dataSource;
    public ExportLatestValuesCsvServlet(DataSource dataSource){
        this.dataSource = dataSource;
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = request.getParameter("fileName");
        CsvExportUtil.exportLatestValuesCsv(response, dataSource, fileName);
    }
}
