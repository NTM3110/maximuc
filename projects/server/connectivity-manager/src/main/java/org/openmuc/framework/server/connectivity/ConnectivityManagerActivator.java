package org.openmuc.framework.server.connectivity;

import org.openmuc.framework.server.connectivity.servlets.VpnConnectionServlet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Declarative Service for Connectivity Manager.
 * Registers REST endpoints for VPN and 4G management.
 */
@Component
public class ConnectivityManagerActivator {

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityManagerActivator.class);
    private static final String VPN_ALIAS = "/api/vpn/connections";
    
    private static HttpService httpService;
    private final VpnConnectionServlet vpnServlet = new VpnConnectionServlet();

    @Reference
    protected void setHttpService(HttpService httpService) {
        ConnectivityManagerActivator.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        ConnectivityManagerActivator.httpService = null;
    }

    @Activate
    protected void activate(ComponentContext context) throws Exception {
        logger.info("Activating Connectivity Manager");

        if (httpService != null) {
            try {
                // Register VPN connection servlet
                httpService.registerServlet(VPN_ALIAS, vpnServlet, null, null);
                logger.info("Registered VPN Connection servlet at {}", VPN_ALIAS);
            } catch (Exception e) {
                logger.error("Failed to register VPN Connection servlet", e);
                throw e;
            }
        } else {
            logger.error("HttpService not available");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        logger.info("Deactivating Connectivity Manager");

        if (httpService != null) {
            try {
                httpService.unregister(VPN_ALIAS);
                logger.info("Unregistered VPN Connection servlet");
            } catch (IllegalArgumentException e) {
                logger.warn("Servlet was not registered: {}", VPN_ALIAS);
            }
        }
    }
}
