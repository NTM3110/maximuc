package org.openmuc.framework.lib.rest1.service.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public final class OsgiJdbcUtil {

    private OsgiJdbcUtil() {}

    public static DataSource getPostgresDataSource(BundleContext ctx, String url, String user, String password)
            throws Exception {

        // Select the correct DataSourceFactory (PostgreSQL)
        String filter = "(osgi.jdbc.driver.class=org.postgresql.Driver)";
        ServiceReference<DataSourceFactory> ref =
                ctx.getServiceReferences(DataSourceFactory.class, filter)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("PostgreSQL DataSourceFactory not found. Is the Postgres JDBC bundle installed/active?"));

        DataSourceFactory factory = ctx.getService(ref);
        if (factory == null) {
            throw new IllegalStateException("Failed to get PostgreSQL DataSourceFactory service.");
        }

        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, url);
        props.setProperty(DataSourceFactory.JDBC_USER, user);
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, password);

        return factory.createDataSource(props);
    }
}

