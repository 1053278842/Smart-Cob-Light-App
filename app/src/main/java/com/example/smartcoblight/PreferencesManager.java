package com.example.smartcoblight;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class PreferencesManager {
    private static final String PREFS_NAME = "SmartCobLightPrefs";

    private static final String KEY_STATUS = "key_status";
    private static final String KEY_ADVANCED_SETTINGS_VISIBLE = "key_advanced_settings_visible";
    // 通用键：存储所有灯的唯一标识列表（用于遍历所有灯）
    private static final String KEY_SELECTED_COLOR = "selected_color";
    private static final String KEY_MQTT_BROKER_URL = "mqtt_broker_url";

    private SharedPreferences preferences;
    private Gson gson;

    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // -------------------------- 灯参数的通用操作（核心改进） --------------------------

    /**
     * 保存单种灯的参数
     *
     * @param lightId  灯的唯一标识（如"cold_light"、"warm_light"、"red_light"等）
     * @param settings 该灯的参数
     */
    public void saveLightSettings(Integer lightId, LightDevice settings) {
        // 1. 保存当前灯的参数（key格式："light_${id}_settings"）
        String key = "light_" + lightId + "_settings";
        String json = gson.toJson(settings);
        preferences.edit().putString(key, json).apply();
    }

    /**
     * 获取单种灯的参数（如果没有存储，返回默认值）
     *
     * @param lightId 灯的唯一标识
     */
    public LightDevice getLightSettings(Integer lightId) {
        String key = "light_" + lightId + "_settings";
        String json = preferences.getString(key, null);
        if (json == null) {
            // 没有存储过，返回默认参数
            return null;
        }
        return gson.fromJson(json, LightDevice.class);
    }

    public boolean getAdvanceSettingsVisible() {
        return preferences.getBoolean(KEY_ADVANCED_SETTINGS_VISIBLE, false);
    }

    public void setAdvanceSettingsVisible(Boolean status) {
        preferences.edit().putBoolean(KEY_ADVANCED_SETTINGS_VISIBLE, status).apply();
    }

    public boolean getStatus() {
        return preferences.getBoolean(KEY_STATUS, false);
    }

    public void setStatus(Boolean status) {
        preferences.edit().putBoolean(KEY_STATUS, status).apply();
    }
    // -------------------------- 其他全局参数（与灯类型无关） --------------------------

    public void saveSelectedColor(int color) {
        preferences.edit().putInt(KEY_SELECTED_COLOR, color).apply();
    }

    public int getSelectedColor() {
        return preferences.getInt(KEY_SELECTED_COLOR, 0xFF0000); // 默认红色
    }

    public void saveMqttBrokerUrl(String url) {
        preferences.edit().putString(KEY_MQTT_BROKER_URL, url).apply();
    }

    public String getMqttBrokerUrl() {
        return preferences.getString(KEY_MQTT_BROKER_URL, "tcp://xxx.xxx.xxx.xxx:xxxx");
    }

    // 清除所有设置
    public void clearAllSettings() {
        preferences.edit().clear().apply();
    }
}
