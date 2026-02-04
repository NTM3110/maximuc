package org.openmuc.framework.lib.rest1.domain.model;

import org.openmuc.framework.lib.rest1.common.enums.DischargeState;
import java.time.LocalDateTime;
import static org.openmuc.framework.lib.rest1.common.enums.DischargeState.PENDING;
import static org.openmuc.framework.lib.rest1.common.enums.Status.ACTIVE;
import org.openmuc.framework.lib.rest1.common.enums.Status;

public class SoHSchedule{
    private Long id;
    private String strId;
    private Double usedQ;
    private Double soh;
    private Double current;
    private Double socBefore;
    private Double socAfter;
    private DischargeState state;
    private Status status;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private LocalDateTime updateDatetime;

    public SoHSchedule(String strId, Double current, LocalDateTime startDatetime) {
        this.strId = strId;
        this.current = current;
        this.startDatetime = startDatetime;
        this.usedQ = 0.0;
        this.state = PENDING;
        this.status = ACTIVE;
    }
    public SoHSchedule(){}
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getStrId() {
        return strId;
    }
    public void setStrId(String strId) {
        this.strId = strId;
    }
    public Double getUsedQ() {
        return usedQ;
    }
    public void setUsedQ(Double usedQ) {
        this.usedQ = usedQ;
    }
    public Double getSoh() {
        return soh;
    }   
    public void setSoh(Double soh) {
        this.soh = soh; 
    }  
    public Double getCurrent() {
        return current;
    }
    public void setCurrent(Double current) {
        this.current = current;
    }
    public Double getSocBefore() {
        return socBefore;
    }
    public void setSocBefore(Double socBefore) {
        this.socBefore = socBefore;
    }
    public Double getSocAfter() {
        return socAfter;
    }
    public void setSocAfter(Double socAfter) {
        this.socAfter = socAfter;
    }
    public DischargeState getState() {
        return state;
    }
    public void setState(DischargeState state) {
        this.state = state;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }
    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }
    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }
    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }
    public LocalDateTime getUpdateDatetime() {
        return updateDatetime;
    }
    public void setUpdateDatetime(LocalDateTime updateDatetime) {
        this.updateDatetime = updateDatetime;
    }
}