package org.openmuc.framework.server.restws.servlets;

import org.openmuc.framework.lib.rest1.ToJson;
import org.openmuc.framework.lib.rest1.common.Constants;

public class CommonResponse {
    public static void toSuccessResult(String description, ToJson json){
        json.addString("code", Constants.API_RESPONSE.RETURN_CODE_SUCCESS);
        json.addBoolean("success", Constants.STATUS_COMMON.RESPONSE_STATUS_TRUE);
        json.addString("description", description);
    }

    public static void toExceptionResult(String errorMessage, ToJson json){
        json.addString("code", Constants.API_RESPONSE.RETURN_CODE_ERROR);
        json.addBoolean("success", Constants.STATUS_COMMON.RESPONSE_STATUS_FALSE);
        json.addString("description", errorMessage);
    }

    public static void toSuccessResultNull(String description, ToJson json){
        json.addString("code", Constants.API_RESPONSE.RETURN_CODE_SUCCESS);
        json.addBoolean("success", Constants.STATUS_COMMON.RESPONSE_STATUS_TRUE);
        json.addString("description", description);
    }
}
