package org.openmuc.framework.lib.rest1.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.openmuc.framework.lib.rest1.domain.dto.ScheduleDTO;
import org.openmuc.framework.lib.rest1.service.SoHService;
import org.openmuc.framework.lib.rest1.service.impl.SoHServiceImpl;

import org.openmuc.framework.lib.rest1.sql.SoHScheduleRepoImpl;
import static org.openmuc.framework.lib.rest1.common.enums.Status.ACTIVE;
import static org.openmuc.framework.lib.rest1.common.enums.DischargeState.RUNNING;;
import static org.openmuc.framework.lib.rest1.common.enums.DischargeState.STOPPED;
import org.openmuc.framework.lib.rest1.domain.model.SoHSchedule;
import java.util.Arrays;    

public class SoHServiceImplTest {

    private SoHService service;

    @BeforeEach
    public void setUp() {
        service = new SoHServiceImpl();
    }

    @Test
    public void testCreateSchedule() {
        // Add test logic here
        LocalDateTime startTime = LocalDateTime.now();
        service.createSchedule("str2", startTime, 50.0);
        System.out.println("testCreateSchedule -> Schedule created for str2 at " + startTime);
        assertTrue(true); // Replace with actual assertions
    }

    @Test
    public void testUpdateSchedule() {
        // Add test logic here
        LocalDateTime newStartTime = LocalDateTime.now();
        service.updateSchedule(5L, newStartTime);
        System.out.println("testUpdateSchedule -> Schedule with id 5 updated to new start time " + newStartTime);
        assertTrue(true); // Replace with actual assertions
    }

    @Test 
    public void testRemoveSchedule(){
        service.removeSchedule(5L);
        System.out.println("testRemoveSchedule -> Schedule with id 5 removed");
        assertTrue(true); // Replace with actual assertions
    }

    @Test
    public void testStopSchedule(){
        service.stopSchedule(6L);
        System.out.println("testStopSchedule -> Schedule with id 6 stopped");
        assertTrue(true); // Replace with actual assertions
    }

    @Test
    public void testGetListSchedule(){
        System.out.println("testGetListSchedule -> Fetching list of schedules");
        List<ScheduleDTO> schedules = service.getListSchedule();
        for(ScheduleDTO schedule : schedules){
            System.out.println("Schedule ID: " + schedule.getId() + ", String ID: " + schedule.getStrId() + ", State: " + schedule.getState());
        }
        // System.out.println("testGetListSchedule -> " + schedules);
        assertNotNull(schedules);
    }

    @Test 
    public void testGetSoHSchedule(){
        SoHScheduleRepoImpl sohScheduleRepoImpl = new SoHScheduleRepoImpl();
        Long id = 9L;
        System.out.println("testGetSoHSchedule -> Fetching schedule with id 9");
        SoHSchedule schedule = sohScheduleRepoImpl.findByIdAndStateInAndStatus(id, Arrays.asList(RUNNING,STOPPED),ACTIVE);
        System.out.println("testGetSoHSchedule:socBefore: -> " + schedule.getSocBefore());
        assertNotNull(schedule);
    }

}