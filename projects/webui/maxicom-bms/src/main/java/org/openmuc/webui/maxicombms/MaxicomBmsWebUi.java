package org.openmuc.webui.maxicombms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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

@Component(service = WebUiPluginService.class)
public final class MaxicomBmsWebUi extends WebUiPluginService {

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
        super.activate(context);
        servlet = new MaxicomBmsServlet();
        try {
            httpService.registerServlet("/maxicom-bms/html", servlet, null, new BundleHttpContext(getContextBundle()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    protected void deactivate() {
        if (httpService != null) {
            httpService.unregister("/maxicom-bms/html");
        }
    }

    private class MaxicomBmsServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = req.getPathInfo();
            if (path == null) {
                path = "/index.html";
            }

            URL resource = getContextBundle().getResource("/html" + path);

            // Fallback to index.html if resource not found (SPA routing)
            if (resource == null) {
                resource = getContextBundle().getResource("/html/index.html");
            }

            if (resource == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String mimeType = getServletContext().getMimeType(path);
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
