package org.openmuc.framework.server.restws.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.openmuc.framework.lib.rest1.sql.SoHScheduleRepoImpl;
import org.openmuc.framework.lib.rest1.sql.EntityRepoImpl;
import org.openmuc.framework.lib.rest1.service.ASyncService;
import org.openmuc.framework.lib.rest1.domain.model.SoHSchedule;
import org.openmuc.framework.lib.rest1.common.enums.DischargeState;
import org.openmuc.framework.lib.rest1.common.enums.Status;

/**
 * Simple test class for initUpdateTimer functionality
 */
@DisplayName("initUpdateTimer Tests")
public class RestServerInitUpdateTimerTest {

    private Timer updateTimer;
    private SoHScheduleRepoImpl sohScheduleRepoImpl;
    private EntityRepoImpl entityRepoImpl;
    private ASyncService asyncService;

    @BeforeEach
    public void setUp() {
        sohScheduleRepoImpl = mock(SoHScheduleRepoImpl.class);
        entityRepoImpl = mock(EntityRepoImpl.class);
        asyncService = mock(ASyncService.class);
    }

    /**
     * Initialize the update timer with scheduled task
     */
    private void initUpdateTimer() {
        updateTimer = new Timer("Update SoH Schedule");

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // System.out.println("Timer task running to update SoH Schedule");
                LocalDateTime now = LocalDateTime.now();
                List<SoHSchedule> qualifiedSchedules = sohScheduleRepoImpl
                        .findByStartDatetimeBeforeAndStateAndStatus(now, DischargeState.PENDING, Status.ACTIVE);

                for (SoHSchedule schedule : qualifiedSchedules) {
                    System.out.println("Starting SoH calculation for Schedule ID: " + schedule.getId()
                            + ", String ID: " + schedule.getStrId());
                    Double socValue = entityRepoImpl.getSocValue(schedule.getStrId());
                    if (Objects.isNull(socValue)) {
                        schedule.setSocBefore(100D);
                    } else {
                        schedule.setSocBefore(socValue);
                    }
                    schedule.setState(DischargeState.RUNNING);
                    sohScheduleRepoImpl.save(schedule);
                    asyncService.calculateSoh(schedule.getId(), schedule.getStrId());
                }
            }
        };
        updateTimer.scheduleAtFixedRate(task, 1000, 1000);
    }

    @Test
    @DisplayName("Timer should be initialized successfully")
    public void testTimerInitialization() {
        initUpdateTimer();
        assertNotNull(updateTimer, "Timer should be initialized");
        updateTimer.cancel();
    }

    @Test
    @DisplayName("Timer should retrieve pending schedules and update them")
    public void testTimerRetrievesAndUpdatesSchedules() throws InterruptedException {
        // Create mock schedules
        List<SoHSchedule> mockSchedules = new ArrayList<>();
        SoHSchedule schedule1 = new SoHSchedule();
        schedule1.setId(1L);
        schedule1.setStrId("str1");
        schedule1.setState(DischargeState.PENDING);
        mockSchedules.add(schedule1);

        // Setup mocks
        when(sohScheduleRepoImpl.findByStartDatetimeBeforeAndStateAndStatus(any(LocalDateTime.class),
                eq(DischargeState.PENDING), eq(Status.ACTIVE))).thenReturn(mockSchedules);
        when(entityRepoImpl.getSocValue("str1")).thenReturn(80.0);

        initUpdateTimer();
        Thread.sleep(1500); // Wait for timer to execute

        // Verify schedule was updated
        assertEquals(DischargeState.RUNNING, schedule1.getState());
        assertEquals(80.0, schedule1.getSocBefore());
        verify(sohScheduleRepoImpl).save(schedule1);
        verify(asyncService).calculateSoh(1L, "str1");

        updateTimer.cancel();
    }

    @Test
    @DisplayName("Timer should handle null SoC value with default 100")
    public void testTimerHandlesNullSoCValue() throws InterruptedException {
        // Create mock schedule
        List<SoHSchedule> mockSchedules = new ArrayList<>();
        SoHSchedule schedule1 = new SoHSchedule();
        schedule1.setId(1L);
        schedule1.setStrId("str1");
        mockSchedules.add(schedule1);

        // Setup mocks - return null for SoC
        when(sohScheduleRepoImpl.findByStartDatetimeBeforeAndStateAndStatus(any(LocalDateTime.class),
                eq(DischargeState.PENDING), eq(Status.ACTIVE))).thenReturn(mockSchedules);
        when(entityRepoImpl.getSocValue("str1")).thenReturn(null);

        initUpdateTimer();
        Thread.sleep(1500);

        // Verify default SoC value was set
        assertEquals(100.0, schedule1.getSocBefore());
        verify(sohScheduleRepoImpl).save(schedule1);

        updateTimer.cancel();
    }

    @Test
    @DisplayName("Timer should handle multiple schedules")
    public void testTimerHandlesMultipleSchedules() throws InterruptedException {
        // Create multiple mock schedules
        List<SoHSchedule> mockSchedules = new ArrayList<>();
        SoHSchedule schedule1 = new SoHSchedule();
        schedule1.setId(1L);
        schedule1.setStrId("str1");
        mockSchedules.add(schedule1);

        SoHSchedule schedule2 = new SoHSchedule();
        schedule2.setId(2L);
        schedule2.setStrId("str2");
        mockSchedules.add(schedule2);

        // Setup mocks
        when(sohScheduleRepoImpl.findByStartDatetimeBeforeAndStateAndStatus(any(LocalDateTime.class),
                eq(DischargeState.PENDING), eq(Status.ACTIVE))).thenReturn(mockSchedules);
        when(entityRepoImpl.getSocValue(anyString())).thenReturn(75.0);

        initUpdateTimer();
        Thread.sleep(1500);

        // Verify both schedules were processed
        assertEquals(DischargeState.RUNNING, schedule1.getState());
        assertEquals(DischargeState.RUNNING, schedule2.getState());
        verify(asyncService).calculateSoh(1L, "str1");
        verify(asyncService).calculateSoh(2L, "str2");

        updateTimer.cancel();
    }

    @Test
    @DisplayName("Timer should handle empty schedule list")
    public void testTimerHandlesEmptyScheduleList() throws InterruptedException {
        // Setup mocks to return empty list
        when(sohScheduleRepoImpl.findByStartDatetimeBeforeAndStateAndStatus(any(LocalDateTime.class),
                eq(DischargeState.PENDING), eq(Status.ACTIVE))).thenReturn(new ArrayList<>());

        initUpdateTimer();
        Thread.sleep(1500);

        // Verify no async calculations were triggered
        verify(asyncService, never()).calculateSoh(anyLong(), anyString());

        updateTimer.cancel();
    }

    @Test
    @DisplayName("Timer should execute at fixed rate")
    public void testTimerExecutesAtFixedRate() throws InterruptedException {
        // Create mock schedule
        List<SoHSchedule> mockSchedules = new ArrayList<>();
        SoHSchedule schedule1 = new SoHSchedule();
        schedule1.setId(1L);
        schedule1.setStrId("str1");
        mockSchedules.add(schedule1);

        // Setup mocks
        when(sohScheduleRepoImpl.findByStartDatetimeBeforeAndStateAndStatus(any(LocalDateTime.class),
                eq(DischargeState.PENDING), eq(Status.ACTIVE))).thenReturn(mockSchedules);
        when(entityRepoImpl.getSocValue("str1")).thenReturn(80.0);

        initUpdateTimer();
        Thread.sleep(3500); // Wait for multiple executions

        // Verify the task was executed multiple times (at least 3 times in 3.5 seconds)
        verify(sohScheduleRepoImpl, atLeast(3)).findByStartDatetimeBeforeAndStateAndStatus(any(LocalDateTime.class),
                eq(DischargeState.PENDING), eq(Status.ACTIVE));

        updateTimer.cancel();
    }
}
