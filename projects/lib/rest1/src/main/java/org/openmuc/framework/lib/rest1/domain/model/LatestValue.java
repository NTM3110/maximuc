package org.openmuc.framework.lib.rest1.domain.model;

public class LatestValue {
    private String channelId;
    private String valueType;
    private Double valueDouble;
    private String valueString;
    private Boolean valueBoolean;
    private String updatedDatetime;

    public String getChannelId() {
        return channelId;
    }   
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    public Double getValueDouble() {
        return valueDouble;
    }
    public void setValueDouble(Double valueDouble) {
        this.valueDouble = valueDouble;
    }
    public String getValueString() {
        return valueString; 
    }
    public void setValueString(String valueString) {
        this.valueString = valueString;
    }
    public Boolean getValueBoolean() {
        return valueBoolean;    
    }
    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }
    public String getUpdatedDatetime() {
        return updatedDatetime;
    }
    public void setUpdatedDatetime(String updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }
}
