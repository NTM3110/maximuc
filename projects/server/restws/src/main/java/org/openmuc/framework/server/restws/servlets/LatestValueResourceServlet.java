package org.openmuc.framework.server.restws.servlets;


import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.server.restws.servlets.GenericServlet;
import org.openmuc.framework.server.restws.servlets.ServletLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openmuc.framework.dataaccess.DataAccessService;
import org.openmuc.framework.config.ConfigService;
import org.openmuc.framework.config.RootConfig;
import org.openmuc.framework.lib.rest1.ToJson;
import org.openmuc.framework.lib.rest1.domain.dto.Account;
import org.openmuc.framework.lib.rest1.domain.dto.StringDetailDTO;
import org.openmuc.framework.lib.rest1.service.LatestValueService;
import org.openmuc.framework.lib.rest1.service.impl.LatestValueServiceImpl;

public class LatestValueResourceServlet extends GenericServlet {
    
    private DataAccessService dataAccess;
    private ConfigService configService;
    private RootConfig rootConfig;

    private static final Logger logger = LoggerFactory.getLogger(LatestValueResourceServlet.class);
    public static final LatestValueService latestValueService = new LatestValueServiceImpl();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
        // INFO: pathAndQueryString[0] = pathInfo
        // INFO: pathAndQueryString[1] = queryString
        if (pathAndQueryString != null) {
            // setConfigAccess();
            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);

            ToJson json = new ToJson();

            //Get Dev Values

            if(pathInfoArray.length == 0){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addString("result","Invalid Latest Value request.");
            }
            else{
                if (pathInfoArray[0].replace("/", "").equals("dev")) {
                    try{
                        Map<String, String> deviceMap = latestValueService.getDevValues();
                        if(deviceMap != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            json.addMap("data", deviceMap);
                        }
                        else{
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            json.addString("result","Dev values not found.");
                        }
                    }catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result",e.getMessage());
                    }
                }

                //Get Site Name
                else if(pathInfoArray[0].replace("/","").equals("site-name")) {
                    try{
                        String siteName = latestValueService.getSiteName();
                        if(siteName != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            json.addString("data", siteName);
                        }
                        else{
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            json.addString("result","Site name not found.");
                        }
                    }catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result",e.getMessage());
                    }
                }

                //Get String Details
                else if(pathInfoArray[0].replace("/","").equals("string")){
                    try{
                        String stringId = request.getParameter("stringId");
                        if(stringId == null){
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            json.addString("result","Missing required parameter: stringId.");
                        }
                        else{
                            System.out.println("Fetching details for stringId: " + stringId);
                            StringDetailDTO string = latestValueService.getStringDetails(stringId);
                            if(string != null) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                json.addObject(string);
                            }
                            else{
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                json.addString("result","String details not found for stringId: " + stringId);
                            }
                        }
                    } catch(Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result",e.getMessage());
                    }
                }

                //GEt Account Details
                else if(pathInfoArray[0].replace("/", "").equals("account")){
            
                    try {
                        String accountIdStr = request.getParameter("accountId");

                        if(accountIdStr == null){
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            json.addString("result","Missing required parameter: accountId.");
                        }
                        else{
                            int accountId = Integer.parseInt(accountIdStr);
                            Account account = latestValueService.getAccountDetails(accountId);
                            if(account != null) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                json.addObject(account);
                            }
                            else{
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                json.addString("result","Account details not found for accountID: " + accountId);
                            }
                        }
                    }catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result", e.getMessage());
                    }
                }
                else{
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result","Invalid GET request for Latest Value Resource Servlet.");
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
            setConfigAccess();
            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
            ToJson json = new ToJson();
            if(pathInfoArray.length == 0){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.addString("result","Invalid Latest Value request.");
            }
            else{
                if(pathInfoArray[0].replace("/", "").equals("delete-string")){
                    try{
                        String strId = request.getParameter("stringId");
                        if(strId == null){
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            json.addString("result","Missing required parameter: stringId.");
                        }
                        else{
                            boolean deleted = latestValueService.deleteString(strId);
                            if (deleted) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                json.addString("result","String deleted successfully");
                            } else {
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                json.addString("result", "String not found");
                            }
                        }
                    }catch(Exception e){
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result",e.getMessage());
                    }
                }
                else{
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result","Invalid POST request for Latest Value Resource Servlet.");
                }
            }
            sendJson(json, response);
        }
    }

    private void setConfigAccess() {
        this.dataAccess = handleDataAccessService(null);
        this.configService = handleConfigService(null);
        this.rootConfig = handleRootConfig(null);
    }
}
