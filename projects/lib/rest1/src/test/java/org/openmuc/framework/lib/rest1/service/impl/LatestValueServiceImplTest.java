package org.openmuc.framework.lib.rest1.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openmuc.framework.lib.rest1.domain.dto.Account;
import org.openmuc.framework.lib.rest1.domain.dto.StringDetailDTO;
import org.openmuc.framework.lib.rest1.service.impl.LatestValueServiceImpl;

public class LatestValueServiceImplTest {

    private LatestValueServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new LatestValueServiceImpl();
    }

    @Test
    public void testGetDevValues() {
        Map<String, String> result = service.getDevValues();
        System.out.println("testGetDevValues -> " + result);
        assertNotNull(result);
        assertEquals("3.0", result.get("dev_serial_comm_number"));
    }

    @Test
    public void testGetSiteName() {
        String result = service.getSiteName();
        System.out.println("testGetSiteName -> " + result);
        assertNotNull(result);
        assertEquals("Site1", result);
    }

    @Test
    public void testGetStringDetails() {
        StringDetailDTO result = service.getStringDetails("1");
        System.out.println("testGetStringDetails -> " + result);
        assertNotNull(result);
        assertEquals("Dan 1", result.getStringName());
        assertEquals("Phoenix", result.getCellBrand());
        assertEquals("PX100", result.getCellModel());
        assertEquals(100.0, result.getCellQty());
        assertEquals(100.0, result.getCNominal());
        assertEquals(2.0, result.getVNominal());
    }

    @Test
    public void testGetAccountDetails() {
        Account result = service.getAccountDetails(1);
        System.out.println("testGetAccountDetails -> " + result);
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("admin1", result.getPassword());
    }

    @Test
    public void testDeleteStringExists() {
        StringDetailDTO str1 = service.getStringDetails("2");
        System.out.println("pre-delete str1 -> " + str1);
        assertNotNull(str1);
        
        boolean result = service.deleteString("2");
        System.out.println("deleteString result -> " + result);
        assertTrue(result);
    }
}

