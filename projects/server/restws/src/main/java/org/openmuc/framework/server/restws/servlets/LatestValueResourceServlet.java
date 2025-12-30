package org.openmuc.framework.server.restws.servlets;


import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.lib.rest1.FromJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openmuc.framework.lib.rest1.ToJson;
import org.openmuc.framework.lib.rest1.domain.dto.Account;
import org.openmuc.framework.lib.rest1.domain.dto.StringDetailDTO;
import org.openmuc.framework.lib.rest1.service.LatestValueService;
import org.openmuc.framework.lib.rest1.service.impl.LatestValueServiceImpl;


import com.google.gson.JsonObject;


public class LatestValueResourceServlet extends GenericServlet {


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
//                json.addString("result","Invalid Latest Value request.");
                CommonResponse.toExceptionResult("Invalid Latest Value request", json);
            }
            else{
                if (pathInfoArray[0].replace("/", "").equals("dev")) {
                    try{
                        Map<String, String> deviceMap = latestValueService.getDevValues();
                        if(deviceMap != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            CommonResponse.toSuccessResult("Dev values found", json);
                            json.addMap("data", deviceMap);
                        }
                        else{
                            response.setStatus(HttpServletResponse.SC_OK);
                            CommonResponse.toSuccessResultNull("Dev values NOT FOUND", json);
//                            json.addMap("data", null);
                        }
                    }catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }
                }

                //Get Site Name
                else if(pathInfoArray[0].replace("/","").equals("site-name")) {
                    try{
                        String siteName = latestValueService.getSiteName();
                        if(siteName != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            CommonResponse.toSuccessResult("Site name found", json);
                            json.addString("data", siteName);
                        }
                        else{
                            response.setStatus(HttpServletResponse.SC_OK);
                            CommonResponse.toSuccessResultNull("Site name NOT FOUND", json);
                        }
                    }catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }
                }

                //Get String Details
                else if(pathInfoArray[0].replace("/","").equals("string")){
                    try{
                        String stringId = request.getParameter("stringID");
                        if(stringId == null){
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                            json.addString("result","Missing required parameter: stringId.");
                            CommonResponse.toExceptionResult("Missing required parameter: stringID", json);
                        }
                        else{
                            System.out.println("Fetching details for stringId: " + stringId);
                            StringDetailDTO stringDetail = latestValueService.getStringDetails(stringId);
                            if(stringDetail != null) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                CommonResponse.toSuccessResult("String details found", json);
                                json.addObject(stringDetail);
                            }
                            else{
                                response.setStatus(HttpServletResponse.SC_OK);
                                CommonResponse.toSuccessResultNull("Details NOT FOUND for string "+ stringId, json);
                            }
                        }
                    } catch(Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                        json.addString("result",e.getMessage());
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }

                }

                //GEt Account Details
                else if(pathInfoArray[0].replace("/", "").equals("account")){
            
                    try {
                        String accountIdStr = request.getParameter("accountID");

                        if(accountIdStr == null){
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                            json.addString("result","Missing required parameter: accountId.");
                            CommonResponse.toExceptionResult("Missing requirement parameter: accountID", json);
                        }
                        else{
                            int accountId = Integer.parseInt(accountIdStr);
                            Account account = latestValueService.getAccountDetails(accountId);
                            if(account != null) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                CommonResponse.toSuccessResult("Account Details Found", json);
                                json.addObject(account);
                            }
                            else{
                                response.setStatus(HttpServletResponse.SC_OK);
//                                json.addString("result","Account details not found for accountID: " + accountId);
                                CommonResponse.toSuccessResultNull("Account details not found for accountID"+ accountId, json);
                            }
                        }
                    }catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                        json.addString("result", e.getMessage());
                        CommonResponse.toExceptionResult(e.getMessage(),json);
                    }
                }
                else{
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                    json.addString("result","Invalid GET request for Latest Value Resource Servlet.");
                    CommonResponse.toExceptionResult("Invalid GET request for Latest Value Resource Servlet",json);
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
//                            json.addString("result","Missing required parameter: stringId.");
                            CommonResponse.toExceptionResult("Missing requirement parameter: stringId", json);
                        }
                        else{
                            boolean deleted = latestValueService.deleteString(strId);
                            if (deleted) {
                                response.setStatus(HttpServletResponse.SC_OK);
//                                json.addString("result","String deleted successfully");
                                CommonResponse.toSuccessResult("String deleted successfully", json);
                            } else {
                                response.setStatus(HttpServletResponse.SC_OK);
//                                json.addString("result", "String not found");
                                CommonResponse.toSuccessResultNull("String to delete not found", json);
                            }
                        }
                    }catch(Exception e){
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                        json.addString("result",e.getMessage());
                        CommonResponse.toExceptionResult(e.getMessage(), json);
                    }
                }
                else{
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                    json.addString("result","Invalid POST request for Latest Value Resource Servlet.");
                    CommonResponse.toExceptionResult("Invalid POST request for Latest Value Resource Servlet", json);
                }
            }
            sendJson(json, response);
        }
    }

//    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json");
//        String[] pathAndQueryString = checkIfItIsACorrectRest(request, response, logger);
//        // INFO: pathAndQueryString[0] = pathInfo
//        // INFO: pathAndQueryString[1] = queryString
//        if(pathAndQueryString != null){
//            String pathInfo = pathAndQueryString[ServletLib.PATH_ARRAY_NR];
//            String[] pathInfoArray = ServletLib.getPathInfoArray(pathInfo);
//            ToJson toJson = new ToJson();
//            FromJson fromJson = ServletLib.getFromJson(request, logger, response);
//            JsonObject jo = fromJson.getJsonObject();
//            jo.get("")
//        }
//    }
}
