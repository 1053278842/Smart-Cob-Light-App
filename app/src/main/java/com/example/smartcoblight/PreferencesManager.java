package com.example.smartcoblight;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PreferencesManager {
    private static final String PREFS_NAME = "SmartCobLightPrefs";
    private static final String KEY_COLD_LIGHT_MIN = "cold_light_min";
    private static final String KEY_COLD_LIGHT_MAX = "cold_light_max";
    private static final String KEY_WARM_LIGHT_MIN = "warm_light_min";
    private static final String KEY_WARM_LIGHT_MAX = "warm_light_max";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_PHASE = "phase";
    private static final String KEY_TYPE = "type";
    private static final String KEY_STATUS = "status";
    private static final String KEY_SELECTED_COLOR = "selected_color";
    private static final String KEY_MQTT_BROKER_URL = "mqtt_broker_url";
    
    private SharedPreferences preferences;
    private Gson gson;
    
    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // 冷灯设置
    public void saveColdLightRange(int min, int max) {
        preferences.edit()
                .putInt(KEY_COLD_LIGHT_MIN, min)
                .putInt(KEY_COLD_LIGHT_MAX, max)
                .apply();
    }
    
    public int getColdLightMin() {
        return preferences.getInt(KEY_COLD_LIGHT_MIN, 20);
    }
    
    public int getColdLightMax() {
        return preferences.getInt(KEY_COLD_LIGHT_MAX, 80);
    }
    
    // 暖灯设置
    public void saveWarmLightRange(int min, int max) {
        preferences.edit()
                .putInt(KEY_WARM_LIGHT_MIN, min)
                .putInt(KEY_WARM_LIGHT_MAX, max)
                .apply();
    }
    
    public int getWarmLightMin() {
        return preferences.getInt(KEY_WARM_LIGHT_MIN, 20);
    }
    
    public int getWarmLightMax() {
        return preferences.getInt(KEY_WARM_LIGHT_MAX, 80);
    }
    
    // 速度设置
    public void saveSpeed(float speed) {
        preferences.edit()
                .putFloat(KEY_SPEED, speed)
                .apply();
    }
    
    public float getSpeed() {
        return preferences.getFloat(KEY_SPEED, 1.0f);
    }
    
    // 相位设置
    public void savePhase(float phase) {
        preferences.edit()
                .putFloat(KEY_PHASE, phase)
                .apply();
    }
    
    public float getPhase() {
        return preferences.getFloat(KEY_PHASE, 0.0f);
    }
    
    // 类型设置
    public void saveType(String type) {
        preferences.edit()
                .putString(KEY_TYPE, type)
                .apply();
    }
    
    public String getType() {
        return preferences.getString(KEY_TYPE, "wave");
    }
    
    // 状态设置
    public void saveStatus(boolean status) {
        preferences.edit()
                .putBoolean(KEY_STATUS, status)
                .apply();
    }
    
    public boolean getStatus() {
        return preferences.getBoolean(KEY_STATUS, false);
    }
    
    // 选中颜色
    public void saveSelectedColor(int color) {
        preferences.edit()
                .putInt(KEY_SELECTED_COLOR, color)
                .apply();
    }
    
    public int getSelectedColor() {
        return preferences.getInt(KEY_SELECTED_COLOR, 0xFF0000); // 默认红色
    }
    
    // MQTT服务器地址
    public void saveMqttBrokerUrl(String url) {
        preferences.edit()
                .putString(KEY_MQTT_BROKER_URL, url)
                .apply();
    }
    
    public String getMqttBrokerUrl() {
        return preferences.getString(KEY_MQTT_BROKER_URL, "tcp://121.36.251.16:1883");
    }
    
    // 保存所有设备配置
    public void saveAllDevices(List<LightDevice> devices) {
        String json = gson.toJson(devices);
        preferences.edit()
                .putString("all_devices", json)
                .apply();
    }
    
    public List<LightDevice> getAllDevices() {
        String json = preferences.getString("all_devices", null);
        if (json == null) {
            // 返回默认设备配置
            List<LightDevice> defaultDevices = new ArrayList<>();
            LightDevice coldLight = new LightDevice(1);
            coldLight.setMinDuty(getColdLightMin());
            coldLight.setMaxDuty(getColdLightMax());
            coldLight.setStatus(getStatus());
            coldLight.setType(getType());
            coldLight.setSpeed(getSpeed());
            coldLight.setPhase(getPhase());
            
            LightDevice warmLight = new LightDevice(2);
            warmLight.setMinDuty(getWarmLightMin());
            warmLight.setMaxDuty(getWarmLightMax());
            warmLight.setStatus(getStatus());
            warmLight.setType(getType());
            warmLight.setSpeed(getSpeed());
            warmLight.setPhase(getPhase());
            
            defaultDevices.add(coldLight);
            defaultDevices.add(warmLight);
            return defaultDevices;
        }
        
        Type type = new TypeToken<List<LightDevice>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // 清除所有设置
    public void clearAllSettings() {
        preferences.edit().clear().apply();
    }
}
