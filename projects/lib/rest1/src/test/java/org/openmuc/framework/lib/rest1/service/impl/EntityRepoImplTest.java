package org.openmuc.framework.lib.rest1.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.lib.rest1.sql.EntityRepoImpl;

public class EntityRepoImplTest {

    private EntityRepoImpl entityRepoImpl;

    @BeforeEach
    public void setUp() {
        entityRepoImpl = new EntityRepoImpl();
    }

    @Test
    public void testGetCurrentValue() {
        Double current = entityRepoImpl.getCurrentValue("str1");
        System.out.println("Current Value: " + current);
        assertNotNull(current);
        assertTrue(true);
    }

    @Test 
    public void testGetSoCValue(){
        Double soc = entityRepoImpl.getSocValue("str1");
        System.out.println("SoC Value: " + soc);
        assertNotNull(soc);
        assertTrue(true);
    }

    @Test 
    public void testGetTemperatureValue(){
        Double temp = entityRepoImpl.getTemperatureValue("str1");
        System.out.println("Temperature Value: " + temp);
        assertNotNull(temp);
        assertTrue(true);
    }
}