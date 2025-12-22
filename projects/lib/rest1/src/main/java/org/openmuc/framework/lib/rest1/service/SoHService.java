package org.openmuc.framework.lib.rest1.service;


import java.time.LocalDateTime;
import java.util.List;
import org.openmuc.framework.lib.rest1.domain.dto.ScheduleDTO;

public interface SoHService { 
    public void createSchedule(String strId, LocalDateTime startTime, Double current);
    public void updateSchedule(Long id, LocalDateTime startTime);
    public void stopSchedule(Long id);
    public void removeSchedule(Long id);
    public List<ScheduleDTO> getListSchedule();
}