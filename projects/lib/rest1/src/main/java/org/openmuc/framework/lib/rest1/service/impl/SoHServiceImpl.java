package org.openmuc.framework.lib.rest1.service.impl;


import static org.openmuc.framework.lib.rest1.common.enums.DischargeState.*;
import static org.openmuc.framework.lib.rest1.common.enums.Status.ACTIVE;
import static org.openmuc.framework.lib.rest1.common.enums.Status.INACTIVE;
// import org.openmuc.framework.lib.rest1.common.enums;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.openmuc.framework.lib.rest1.domain.dto.ScheduleDTO;
import org.openmuc.framework.lib.rest1.domain.model.SoHSchedule;
import org.openmuc.framework.lib.rest1.sql.SoHScheduleRepoImpl;
import org.openmuc.framework.lib.rest1.service.SoHService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp; 
import java.util.ArrayList;



public class SoHServiceImpl implements SoHService {
    private final SoHScheduleRepoImpl sohScheduleRepoImpl = new SoHScheduleRepoImpl();

    @Override
    public void createSchedule(String strId, LocalDateTime startTime, Double current){
        boolean alreadyExist = checkAvailableSchedule(strId);
        if(alreadyExist) throw new RuntimeException("There is already an available SoH schedule for string: " + strId);

        SoHSchedule sohSchedule = new SoHSchedule(strId, current, startTime);
        sohScheduleRepoImpl.save(sohSchedule);
    }

    @Override
    public void updateSchedule(Long id, LocalDateTime startTime){
        SoHSchedule pendingSchedule = sohScheduleRepoImpl.findByIdAndStateAndStatus(id, PENDING, ACTIVE);
        if(pendingSchedule == null) throw new RuntimeException("No pending SoH schedule found with id: " + id);
        pendingSchedule.setStartDatetime(startTime);
        sohScheduleRepoImpl.save(pendingSchedule);
    }

    @Override
    public void stopSchedule(Long id){
        SoHSchedule runningSchedule = sohScheduleRepoImpl.findByIdAndStateAndStatus(id, RUNNING, ACTIVE);
        if(runningSchedule == null) throw new RuntimeException("No running SoH schedule found with id: " + id);
        runningSchedule.setState(STOPPED);
        sohScheduleRepoImpl.save(runningSchedule);
    }

    @Override
    public void removeSchedule(Long id){
        SoHSchedule pendingSchedule = sohScheduleRepoImpl.findByIdAndStateAndStatus(id, PENDING, ACTIVE);
        if(pendingSchedule == null) throw new RuntimeException("No pending SoH schedule found with id: " + id);
        pendingSchedule.setStatus(INACTIVE);
        sohScheduleRepoImpl.save(pendingSchedule);
    }

    @Override
    public List<ScheduleDTO> getListSchedule(){
        List<ScheduleDTO> result = new ArrayList<>();
        List<SoHSchedule> activeSchedules = sohScheduleRepoImpl.findByStatus(ACTIVE);
        for(SoHSchedule schedule : activeSchedules){
            ScheduleDTO scheduleDTO = new ScheduleDTO(schedule.getId(), schedule.getStrId(), schedule.getCurrent(), schedule.getSoh(), schedule.getState(), schedule.getStartDatetime(), schedule.getEndDatetime());
            result.add(scheduleDTO);
        }
        return result;
    }

    private boolean checkAvailableSchedule(String strId){
        SoHSchedule availableSchedule = sohScheduleRepoImpl.findByStrIdAndStateInAndStatus(strId, Arrays.asList(PENDING, RUNNING), ACTIVE);
        return availableSchedule != null;
    }
}


