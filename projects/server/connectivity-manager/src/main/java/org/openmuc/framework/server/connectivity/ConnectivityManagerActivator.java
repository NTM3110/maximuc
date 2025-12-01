package org.openmuc.framework.server.connectivity;

import org.openmuc.framework.server.connectivity.servlets.VpnConnectionServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Bundle Activator for Connectivity Manager.
 * Registers REST endpoints for VPN and 4G management.
 */
public class ConnectivityManagerActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityManagerActivator.class);
    private static final String VPN_ALIAS = "/api/vpn/connections";

    private HttpService httpService;
    private final VpnConnectionServlet vpnServlet = new VpnConnectionServlet();

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Connectivity Manager Bundle");

        // Get HttpService
        ServiceReference<HttpService> serviceRef = context.getServiceReference(HttpService.class);
        if (serviceRef != null) {
            httpService = context.getService(serviceRef);

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
        } else {
            logger.error("HttpService reference not found");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Connectivity Manager Bundle");

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
