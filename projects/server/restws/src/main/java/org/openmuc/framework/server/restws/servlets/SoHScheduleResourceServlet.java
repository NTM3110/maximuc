package org.openmuc.framework.server.restws.servlets;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.server.restws.servlets.GenericServlet;
import org.openmuc.framework.server.restws.servlets.ServletLib;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.lib.rest1.ToJson;
import org.openmuc.framework.lib.rest1.service.SoHService;
import org.openmuc.framework.lib.rest1.service.impl.SoHServiceImpl; 

import java.util.List;
import org.openmuc.framework.lib.rest1.domain.dto.ScheduleDTO;

public class SoHScheduleResourceServlet extends GenericServlet {

    private DataAccessService dataAccess;
    private ConfigService configService;
    private RootConfig rootConfig;

    private static final Logger logger = LoggerFactory.getLogger(SoHScheduleResourceServlet.class);
    public static final SoHService sohService = new SoHServiceImpl();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        // INFO: pathAndQueryString[0] = pathInfo
        // INFO: pathAndQueryString[1] = queryString
        if (pathAndQueryString != null) {
            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

            // Implement SoH Schedule related GET handling here
            ToJson json = new ToJson();

            if(pathInfoArray[0].replace("/", "").equals("get-list")){
                try{
                    List<ScheduleDTO> schedules = sohService.getListSchedule();
                    if(schedules != null && !schedules.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        json.addObjectList("data", schedules);
                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        json.addString("result","No SoH schedules found.");
                    }
                }catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result",e.getMessage());
                }
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        // INFO: pathAndQueryString[0] = pathInfo
        // INFO: pathAndQueryString[1] = queryString
        if (pathAndQueryString != null) {
            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

            // Implement SoH Schedule related POST handling here
            ToJson json = new ToJson();

            if(pathInfoArray[0].replace("/", "").equals("create")){
                try{
                    // Extract parameters from request (e.g., stringId, startTime, current)
                    String stringId = request.getParameter("stringId");
                    String startTimeStr = request.getParameter("startTime");
                    String currentStr = request.getParameter("current");

                    if(stringId == null || startTimeStr == null || currentStr == null){
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result","Missing required parameters.");
                    } else {
                        Double current = Double.parseDouble(currentStr);
                        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);

                        sohService.createSchedule(stringId, startTime, current);
                        response.setStatus(HttpServletResponse.SC_OK);
                        json.addString("result","SoH schedule created successfully.");
                    }
                }catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result",e.getMessage());
                }
            }
        }
    }
}