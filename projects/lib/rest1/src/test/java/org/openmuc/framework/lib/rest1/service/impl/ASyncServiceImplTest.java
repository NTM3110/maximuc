package org.openmuc.framework.lib.rest1.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.lib.rest1.service.ASyncService;
import org.openmuc.framework.lib.rest1.service.impl.ASyncServiceImpl;

public class ASyncServiceImplTest {
    // Test cases would go here

    private ASyncService asyncService;
    @BeforeEach
    public void setUp() {
        asyncService = new ASyncServiceImpl();
    }

    @Test
    public void testCalculateSoh() {
        Long testId = 9L;
        String testStrId = "str2";
        try{
            System.out.println(" AsyncService returns" + asyncService.calculateSoh(testId, testStrId).get());
        } catch (Exception e){
            System.out.println("Exception occurred during calculateSoh: " + e.getMessage());
        }
    }
}