package com.example.smartcoblight;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LightControlRequest {
    @SerializedName("freelog")
    private boolean freeLog = false;
    
    @SerializedName("device")
    private List<LightDevice> devices;
    
    public LightControlRequest() {
    }
    
    public LightControlRequest(List<LightDevice> devices) {
        this.devices = devices;
    }
    
    public boolean isFreeLog() {
        return freeLog;
    }
    
    public void setFreeLog(boolean freeLog) {
        this.freeLog = freeLog;
    }
    
    public List<LightDevice> getDevices() {
        return devices;
    }
    
    public void setDevices(List<LightDevice> devices) {
        this.devices = devices;
    }
}
