package org.openmuc.framework.driver.simpledriver;

import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;
import org.openmuc.framework.driver.spi.DriverDeviceScanListener;
import org.openmuc.framework.driver.spi.DriverService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmuc.framework.config.ScanInterruptedException;
import org.openmuc.framework.config.DriverInfo;

@Component
public class SimpleDriver implements DriverService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleDriver.class);

    @Override
    public DriverInfo getInfo() {
        final String ID = "simpledriver";
        final String DESCRIPTION = "Simple driver for testing.";
        logger.info("---------------- New Simple Driver -----------");
        return new DriverInfo(ID, DESCRIPTION, "", "", "", "");
    }

    @Override
    public Connection connect(String deviceAddress, String settings) throws ConnectionException {
        logger.info("New Simple Connection on device: {}", deviceAddress);
        return null;
    }

    @Override
    public void scanForDevices(String settings, DriverDeviceScanListener listener) throws ScanInterruptedException {
        logger.info("Scan devices not supported.");
    }

    @Override
    public void interruptDeviceScan() {
        logger.info("intterrupt DEvice scan");
    }
}
