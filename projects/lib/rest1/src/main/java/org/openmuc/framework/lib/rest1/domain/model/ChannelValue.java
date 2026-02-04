package org.openmuc.framework.lib.rest1.domain.model;

import java.time.LocalDateTime;

public class ChannelValue{

    private LocalDateTime time;
    private Integer flag;
    private Integer value;
    public LocalDateTime getTime() {
        return time;
    }
    public void setTime(LocalDateTime time) {
        this.time = time;
    }
    public Integer getFlag() {
        return flag;
    }
    public void setFlag(Integer flag) {
        this.flag = flag;
    }
    public Integer getValue() {
        return value;
    }
    public void setValue(Integer value) {
        this.value = value;
    }
}