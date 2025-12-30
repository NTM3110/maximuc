package org.openmuc.framework.server.restws.servlets;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.server.restws.servlets.GenericServlet;
import org.openmuc.framework.server.restws.servlets.ServletLib;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.Instant;

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
            if (pathInfoArray.length == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                json.addString("result", "Invalid SoH schedule request.");
                CommonResponse.toExceptionResult("Invalid SoH schedule request", json);
            } else {
                if (pathInfoArray[0].replace("/", "").equals("get-list")) {
                 try{
                    List<ScheduleDTO> schedules = sohService.getListSchedule();
                    for (ScheduleDTO schedule : schedules) {
                        System.out.println("Schedule ID: " + schedule.getId() + ", String ID: " + schedule.getStrId()
                                + ", State: " + schedule.getState());
                    }
                    if (schedules != null && !schedules.isEmpty()) {
                        System.out.println("\nFound schedules!!\n");
                        response.setStatus(HttpServletResponse.SC_OK);
                        CommonResponse.toSuccessResult("SoH schedule FOUND", json);
                        json.addObjectList("data", schedules);
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
//                        json.addString("result", "No SoH schedules found.");
                        CommonResponse.toSuccessResultNull("No SoH Schedule FOUND", json);
                    }
                 }catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                    json.addString("result",e.getMessage());
                    CommonResponse.toExceptionResult(e.getMessage(), json);
                 }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    CommonResponse.toExceptionResult("Invalid SoH schedule request",json);
//                    json.addString("result", "Invalid SoH schedule request.");
                }
            }
            sendJson(json, response);
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
            if (pathInfoArray.length == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                json.addString("result", "Invalid SoH schedule request.");
                CommonResponse.toExceptionResult("Invalid SoH schedule request",json);
            } else {
                if (pathInfoArray[0].replace("/", "").equals("create")) {
                    try {
                        // Extract parameters from request (e.g., strId, startTime, current)
                        String stringId = request.getParameter("strId");
                        String startTimeStr = request.getParameter("startTime");
                        String currentStr = request.getParameter("current");

                        if (stringId == null || startTimeStr == null || currentStr == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                            json.addString("result", "Missing required parameters.");
                            CommonResponse.toExceptionResult("Missing required parameters.", json);
                        } else {
                            Instant startTimeInstant = Instant.parse(startTimeStr);
                            Double current = Double.parseDouble(currentStr);
                            LocalDateTime startTime = LocalDateTime.ofInstant(startTimeInstant, ZoneId.of("Asia/Ho_Chi_Minh"));

                            sohService.createSchedule(stringId, startTime, current);
                            response.setStatus(HttpServletResponse.SC_OK);
//                            json.addString("result", "SoH schedule created successfully.");
                            CommonResponse.toSuccessResultNull("SoH schedule created successfully",json);
                        }
                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                        json.addString("result", e.getMessage());
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }

                }

                else if (pathInfoArray[0].replace("/", "").equals("update")) {
                    try {
                        String idStr = request.getParameter("id");
                        String startTimeStr = request.getParameter("startTime");
                        if (idStr == null || startTimeStr == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                            json.addString("result", "Missing required parameters.");
                            CommonResponse.toExceptionResult("Missing required parameter", json);
                        } else {
                            Instant startTimeInstant = Instant.parse(startTimeStr);
                            Long id = Long.parseLong(idStr);
                            sohService.updateSchedule(id, LocalDateTime.ofInstant(startTimeInstant, ZoneId.of("Asia/Ho_Chi_Minh")));

                            response.setStatus(HttpServletResponse.SC_OK);
//                            json.addString("result", "SoH schedule updated successfully");
                            CommonResponse.toSuccessResultNull("SoH schedule updated successfully",json);

                        }
                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        CommonResponse.toExceptionResult(e.getMessage(), json);
//                        json.addString("result", e.getMessage());
                    }

                } else if (pathInfoArray[0].replace("/", "").equals("stop")) {
                    try {
                        String idStr = request.getParameter("id");
                        if (idStr == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                            json.addString("result", "Missing required parameter: id.");
                            CommonResponse.toExceptionResult("Missing required parameter: id", json);
                        } else {
                            Long id = Long.parseLong(idStr);
                            sohService.stopSchedule(id);
                            response.setStatus(HttpServletResponse.SC_OK);
//                            json.addString("result", "SoH schedule stopped successfully");
                            CommonResponse.toSuccessResultNull("Soh schedule stopped successfully", json);
                        }
                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                        json.addString("result", e.getMessage());
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }
                } else if (pathInfoArray[0].replace("/", "").equals("delete")) {
                    try {
                        String idStr = request.getParameter("id");
                        if (idStr == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                            json.addString("result", "Missing required parameter: id.");
                            CommonResponse.toExceptionResult("Missing required parameter: id", json);
                        } else {
                            Long id = Long.parseLong(idStr);
                            sohService.removeSchedule(id);
                            response.setStatus(HttpServletResponse.SC_OK);
//                            json.addString("result", "SoH schedule removed successfully");
                            CommonResponse.toSuccessResultNull("SoH schedule removed successfully", json);
                        }
                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                        json.addString("result", e.getMessage());
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                    json.addString("result", "Invalid SoH schedule request.");
                    CommonResponse.toExceptionResult("Invalid SoH schedule request", json);
                }
            }
            sendJson(json, response);
        }
    }
}