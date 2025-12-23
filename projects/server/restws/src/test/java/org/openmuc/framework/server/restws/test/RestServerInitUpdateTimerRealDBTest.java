package org.openmuc.framework.server.restws.test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Objects;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.openmuc.framework.lib.rest1.sql.SoHScheduleRepoImpl;
import org.openmuc.framework.lib.rest1.sql.EntityRepoImpl;
import org.openmuc.framework.lib.rest1.service.impl.ASyncServiceImpl;
import org.openmuc.framework.lib.rest1.domain.model.SoHSchedule;
import org.openmuc.framework.lib.rest1.common.enums.DischargeState;
import org.openmuc.framework.lib.rest1.common.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test class for initUpdateTimer with REAL DATABASE
 * 
 * This test uses the actual database to test SoH Schedule ID = 8 with strId = str1
 * The timer will keep running until you type 'stop' in the console
 */
@DisplayName("initUpdateTimer Real Database Tests")
public class RestServerInitUpdateTimerRealDBTest {

    private Timer updateTimer;
    private SoHScheduleRepoImpl sohScheduleRepoImpl = new SoHScheduleRepoImpl();
    private EntityRepoImpl entityRepoImpl = new EntityRepoImpl();
    private ASyncServiceImpl asyncService = new ASyncServiceImpl();
    private volatile boolean shouldStop = false;

    @BeforeEach
    public void setUp() {
        // Use real implementations with actual database
        sohScheduleRepoImpl = new SoHScheduleRepoImpl();
        entityRepoImpl = new EntityRepoImpl();
        asyncService = new ASyncServiceImpl();
    }

    /**
     * Initialize the update timer with scheduled task
     */
    private void initUpdateTimer() {
        updateTimer = new Timer("Update SoH Schedule", true); // daemon thread

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (shouldStop) {
                    this.cancel();
                    return;
                }

                try {
                    System.out.println("\n[" + LocalDateTime.now() + "] Timer task running to update SoH Schedule");
                    LocalDateTime now = LocalDateTime.now();
                    List<SoHSchedule> qualifiedSchedules = sohScheduleRepoImpl
                            .findByStartDatetimeBeforeAndStateAndStatus(now, DischargeState.PENDING, Status.ACTIVE);

                    System.out.println("Found " + qualifiedSchedules.size() + " pending schedules");

                    for (SoHSchedule schedule : qualifiedSchedules) {
                        System.out.println("\n>>> Starting SoH calculation for Schedule ID: " + schedule.getId()
                                + ", String ID: " + schedule.getStrId());

                        Double socValue = entityRepoImpl.getSocValue(schedule.getStrId());
                        System.out.println("    Current SoC value: " + socValue);

                        if (Objects.isNull(socValue)) {
                            schedule.setSocBefore(100D);
                            System.out.println("    Set SoC before to default: 100.0");
                        } else {
                            schedule.setSocBefore(socValue);
                            System.out.println("    Set SoC before to: " + socValue);
                        }

                        schedule.setState(DischargeState.RUNNING);
                        schedule.setUpdateDatetime(LocalDateTime.now());
                        sohScheduleRepoImpl.save(schedule);
                        System.out.println("    Schedule state updated to RUNNING");

                        // Trigger async calculation
                        System.out.println("    Triggering async SoH calculation...");
                        asyncService.calculateSoh(schedule.getId(), schedule.getStrId());
                    }

                } catch (Exception e) {
                    System.err.println("Error in timer task: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        // Schedule at fixed rate: initial delay 1 second, repeat every 1 second
        updateTimer.scheduleAtFixedRate(task, 1000, 1000);
        System.out.println("Timer initialized and scheduled at fixed rate (1 second interval)");
    }

    /**
     * Test with real database - Schedule ID 8, String ID str1
     * 
     * This test will:
     * 1. Start the timer
     * 2. Keep running until you type 'stop' in the console
     * 3. Properly cancel the timer on shutdown
     */
    @Test
    @DisplayName("Test with Real Database - Schedule ID 8, String ID str1")
    public void testWithRealDatabase() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("Starting Real Database Test");
        System.out.println("Testing Schedule ID: 8, String ID: str1");
        System.out.println("========================================");
        System.out.println("The timer will run every 1 second.");
        System.out.println("Type 'stop' and press Enter to stop the timer.");
        System.out.println("========================================\n");

        // Initialize and start the timer
        initUpdateTimer();

        // Add shutdown hook to ensure timer is cancelled on JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutdown hook triggered - cancelling timer...");
            if (updateTimer != null) {
                updateTimer.cancel();
                System.out.println("Timer cancelled successfully");
            }
        }));

        // Keep the test running and listen for user input
        try (Scanner scanner = new Scanner(System.in)) {
            while (!shouldStop) {
                if(scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    if (input.equalsIgnoreCase("stop")) {
                        System.out.println("\nStop command received. Shutting down timer...");
                        shouldStop = true;
                        Thread.sleep(2000); // Give timer task time to finish
                        updateTimer.cancel();
                        System.out.println("Timer cancelled successfully");
                        break;
                    } else {
                        System.out.println("Unknown command: " + input + ". Type 'stop' to exit.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure timer is cancelled
            if (updateTimer != null) {
                updateTimer.cancel();
            }
            System.out.println("\nTest completed");
        }
    }

    /**
     * Alternative test that runs for a fixed duration (e.g., 30 seconds)
     * Useful if you want automated testing without manual intervention
     */
    @Test
    @DisplayName("Test with Real Database - Auto Stop After 30 Seconds")
    public void testWithRealDatabaseAutoStop() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("Starting Real Database Test (Auto-stop)");
        System.out.println("Testing Schedule ID: 8, String ID: str1");
        System.out.println("Duration: 30 seconds");
        System.out.println("========================================\n");

        // Initialize and start the timer
        initUpdateTimer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutdown hook triggered - cancelling timer...");
            if (updateTimer != null) {
                updateTimer.cancel();
                System.out.println("Timer cancelled successfully");
            }
        }));

        try {
            // Run for 30 seconds
            long startTime = System.currentTimeMillis();
            long duration = 30000; // 30 seconds

            while ((System.currentTimeMillis() - startTime) < duration) {
                Thread.sleep(1000);
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("[" + (elapsed / 1000) + "s] Timer running...");
            }

            System.out.println("\n30 seconds elapsed. Stopping timer...");
            shouldStop = true;
            Thread.sleep(2000);
            updateTimer.cancel();
            System.out.println("Timer cancelled successfully");

        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
            shouldStop = true;
            updateTimer.cancel();
        } finally {
            if (updateTimer != null) {
                updateTimer.cancel();
            }
            System.out.println("Test completed");
        }
    }

    /**
     * Test that specifically targets Schedule ID 8
     */
    @Test
    @DisplayName("Test Specific Schedule ID 8 with Real Database")
    public void testSpecificScheduleId8() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("Testing Specific Schedule ID: 8");
        System.out.println("========================================\n");

        // Fetch the specific schedule
        SoHSchedule schedule = sohScheduleRepoImpl.findByIdAndStateInAndStatus(8L, 
                java.util.Arrays.asList(DischargeState.PENDING, DischargeState.RUNNING), 
                Status.ACTIVE);

        if (schedule == null) {
            System.out.println("Schedule ID 8 not found in PENDING or RUNNING state");
            return;
        }

        System.out.println("Found Schedule:");
        System.out.println("  ID: " + schedule.getId());
        System.out.println("  String ID: " + schedule.getStrId());
        System.out.println("  State: " + schedule.getState());
        System.out.println("  Status: " + schedule.getStatus());
        System.out.println("  Start DateTime: " + schedule.getStartDatetime());
        System.out.println("  SoC Before: " + schedule.getSocBefore());

        // Get current SoC value
        Double currentSoC = entityRepoImpl.getSocValue(schedule.getStrId());
        System.out.println("  Current SoC from Entity: " + currentSoC);

        // Update schedule
        if (currentSoC != null) {
            schedule.setSocBefore(currentSoC);
        } else {
            schedule.setSocBefore(100.0);
        }

        schedule.setState(DischargeState.RUNNING);
        schedule.setUpdateDatetime(LocalDateTime.now());
        sohScheduleRepoImpl.save(schedule);

        System.out.println("\nSchedule updated and saved");
        System.out.println("Triggering async SoH calculation...");

        // Trigger async calculation
        asyncService.calculateSoh(schedule.getId(), schedule.getStrId());

        System.out.println("Async calculation triggered");
        System.out.println("Check the logs for calculation progress...");

        // Wait a bit to see if calculation starts
        Thread.sleep(5000);
    }
}
