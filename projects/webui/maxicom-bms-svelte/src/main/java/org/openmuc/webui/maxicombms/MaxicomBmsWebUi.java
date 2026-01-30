package org.openmuc.webui.maxicombms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmuc.framework.webui.spi.WebUiPluginService;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = WebUiPluginService.class)
public final class MaxicomBmsWebUi extends WebUiPluginService {

    private static final Logger logger = LoggerFactory.getLogger(MaxicomBmsWebUi.class);

    @Reference
    private HttpService httpService;

    private MaxicomBmsServlet servlet;

    @Override
    public String getAlias() {
        return "maxicom-bms";
    }

    @Override
    public String getName() {
        return "Maxicom BMS";
    }

    @Override
    public Map<String, String> getResources() {
        // Return empty map to prevent WebUiBase from registering default resources
        return new HashMap<>();
    }

    @Activate
    @Override
    protected void activate(ComponentContext context) {
        logger.warn("Activating MaxicomBmsWebUi-Svelte");
        super.activate(context);
        servlet = new MaxicomBmsServlet();
        try {
            httpService.registerServlet("/", servlet, null, new BundleHttpContext(getContextBundle()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    protected void deactivate() {
        if (httpService != null) {
            httpService.unregister("/");
        }
    }

    private class MaxicomBmsServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // When servlet is at "/", getPathInfo() returns the path after "/"
            // e.g., for "/assets/config/app-config.json", getPathInfo() returns
            // "/assets/config/app-config.json"
            String path = req.getPathInfo();

            // If pathInfo is null (shouldn't happen for "/" servlet), fall back to parsing
            // URI
            if (path == null) {
                String uri = req.getRequestURI();
                String contextPath = req.getContextPath();
                path = uri.substring(contextPath.length());
            }

            if (path.isEmpty() || "/".equals(path)) {
                path = "/index.html";
            }

            // Decode URL-encoded characters (e.g., %20 -> space)
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

            // Determine if this is an asset (file with extension) or a route
            boolean isAsset = decodedPath.lastIndexOf('.') > decodedPath.lastIndexOf('/');

            // Debug logging
            // logger.info("[MaxicomBmsServlet] Requested path: {}, decoded: {}, isAsset: {}", path, decodedPath, isAsset);

            URL resource = getContextBundle().getResource("/html" + decodedPath);

            // Debug logging
            // logger.info("[MaxicomBmsServlet] Resource URL: {}", resource);

            if (resource == null) {
                if (!isAsset) {
                    // For routes without extensions, serve index.html (Angular routing)
                    resource = getContextBundle().getResource("/html/index.html");
                    decodedPath = "/index.html";
                } else {
                    // For missing assets, return 404
                    logger.warn("[MaxicomBmsServlet] Asset not found: /html{}", decodedPath);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            // Set MIME type
            String mimeType = getServletContext().getMimeType(decodedPath);
            // String mimeType = null;
            if (mimeType == null) {
                if (decodedPath.endsWith(".js"))
                    mimeType = "text/javascript";
                else if (decodedPath.endsWith(".css"))
                    mimeType = "text/css";
                else if (decodedPath.endsWith(".html"))
                    mimeType = "text/html";
                else if (decodedPath.endsWith(".json"))
                    mimeType = "application/json";
                else if (decodedPath.endsWith(".svg"))
                    mimeType = "image/svg+xml";
                else if (decodedPath.endsWith(".png"))
                    mimeType = "image/png";
                else if (decodedPath.endsWith(".jpg") || decodedPath.endsWith(".jpeg"))
                    mimeType = "image/jpeg";
                else if (path.endsWith(".woff"))
                    mimeType = "font/woff";
                else if (path.endsWith(".woff2"))
                    mimeType = "font/woff2";
            }
            if (mimeType != null) {
                resp.setContentType(mimeType);
            }

            try (InputStream in = resource.openStream(); OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    private class BundleHttpContext implements HttpContext {
        private final Bundle bundle;

        public BundleHttpContext(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
            return true;
        }

        @Override
        public URL getResource(String name) {
            return bundle.getResource(name);
        }

        @Override
        public String getMimeType(String name) {
            return null;
        }
    }

}
