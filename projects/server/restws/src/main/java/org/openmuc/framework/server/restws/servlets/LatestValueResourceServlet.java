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
    public LatestValueService latestValueService;

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
            if (pathInfoArray[0].replace("/", "").equals("dev")) {
                Map<String, String> deviceMap = latestValueService.getDevValues();
                if(deviceMap != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addMap("data", deviceMap);
                }
                else{
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addString("result","Dev values not found.");
                }
            }
            else if(pathInfoArray[0].replace("/","").equals("site-name")) {
                String siteName = latestValueService.getSiteName();
                if(siteName != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    json.addString("data", siteName);
                }
                else{
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    json.addString("result","Site name not found.");
                }
            }
            else if(pathInfoArray[0].replace("/","").equals("string")){
                if(pathInfoArray.length != 2){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result","Invalid URL. Please provide a string ID.");
                }
                else{
                    String stringId = pathInfoArray[1].replace("/", "");
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
            }
            else if(pathInfoArray[0].replace("/", "").equals("account")){
                if(pathInfoArray.length != 2){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result","Invalid URL. Please provide an account ID.");
                }
                else{
                    int accountID;
                    try {
                        accountID = Integer.parseInt(pathInfoArray[1].replace("/", ""));
                    } catch (NumberFormatException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        json.addString("result","Invalid account ID format. Please provide a numeric account ID.");
                        sendJson(json, response);
                        return;
                    }
                    Account account = latestValueService.getAccountDetails(accountID);
                    if(account != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        json.addObject(account);
                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        json.addString("result","Account details not found for accountID: " + accountID);
                    }
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
            if(pathInfoArray[0].replace("/", "").equals("delete-string")){
                if(pathInfoArray.length != 2){
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.addString("result","Invalid URL. Please provide a string ID to delete.");
                }
                else{
                    boolean deleted = latestValueService.deleteString(pathInfoArray[1]);
                    response.setStatus(HttpServletResponse.SC_OK);
                    if (deleted) {
                        json.addString("result","String deleted successfully");
                    } else {
                        json.addString("result", "String not found");
                    }
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
