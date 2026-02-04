package org.openmuc.framework.lib.rest1.domain.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.openmuc.framework.lib.rest1.common.enums.DischargeState;


public class ScheduleDTO implements Serializable {
    private long id;
    private String strId;
    private Double current;
    private Double soh;
    private DischargeState state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ScheduleDTO(Long id, String strId, Double current, Double soh, DischargeState state, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.strId = strId;
        this.current = current;
        this.soh = soh;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getStrId() {
        return strId;
    }
    public void setStrId(String strId) {
        this.strId = strId;
    }
    public Double getCurrent() {
        return current;
    }
    public void setCurrent(Double current) {
        this.current = current;
    }
    public Double getSoh() {
        return soh;
    }   
    public void setSoh(Double soh) {
        this.soh = soh;
    }
    public DischargeState getState() {
        return state;
    }
    public void setState(DischargeState state) {
        this.state = state;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}