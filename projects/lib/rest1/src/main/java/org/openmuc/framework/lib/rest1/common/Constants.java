package org.openmuc.framework.lib.rest1.common;

public interface Constants {
    final class ASYNC_PARAMS {
        public static final int INTERVAL = 5;
    }
    public static class API_RESPONSE {
        private API_RESPONSE() {
            throw new IllegalStateException();
        }

        public static final String RETURN_CODE_SUCCESS = "200";
        public static final String RETURN_CODE_ERROR = "400";
        public static final String RETURN_CODE_ERROR_NOTFOUND = "404";
        public static final Boolean STATUS_TRUE = true;
        public static final Boolean STATUS_FALSE = false;
        public static final String RETURN_DES_FAILURE_NOTFOUND = "Not Found";
        public static final String RETURN_DES_ERROR = "error";
        public static final String RETURN_DES_SUCCESS = "success";
    }

    public static class STATUS_COMMON {
        public static final Boolean RESPONSE_STATUS_TRUE = true;
        public static final Boolean RESPONSE_STATUS_FALSE = false;
    }
}
