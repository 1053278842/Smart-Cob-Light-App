package com.example.smartcoblight;

import com.google.gson.annotations.SerializedName;

public class LightDevice {
    @SerializedName("id")
    private int id;

    @SerializedName("status")
    private boolean status;

    @SerializedName("type")
    private String type;

    @SerializedName("minduty")
    private float minDuty;

    @SerializedName("maxduty")
    private float maxDuty;

    @SerializedName("speed")
    private float speed;

    @SerializedName("phase")
    private float phase;

    public LightDevice() {
        this.status = false;
        this.type = "波浪";
        this.minDuty = 0f;
        this.maxDuty = 100f;
        this.speed = 1.0f;
        this.phase = 0.0f;
    }

    public LightDevice(int id) {
        this();
        this.id = id;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getMinDuty() {
        return minDuty;
    }

    public void setMinDuty(float minDuty) {
        this.minDuty = minDuty;
    }

    public float getMaxDuty() {
        return maxDuty;
    }

    public void setMaxDuty(float maxDuty) {
        this.maxDuty = maxDuty;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getPhase() {
        return phase;
    }

    public void setPhase(float phase) {
        this.phase = phase;
    }
}
