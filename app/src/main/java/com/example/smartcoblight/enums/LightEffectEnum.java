package com.example.smartcoblight.enums;

/**
 * 灯光模式枚举类
 * 包含模式英文标识和中文描述
 */
public enum LightEffectEnum {
    // 波浪模式
    WAVE("wave", "波浪"),
    // 呼吸模式
    BREATH("breath", "呼吸"),
    // 火焰模式
    FIRE("fire", "火焰"),
    // 渐变模式
    FADE("fade", "渐变");

    // 英文标识（用于代码逻辑、存储等）
    private final String code;
    // 中文描述（用于UI展示）
    private final String desc;

    // 构造方法
    LightEffectEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据英文标识获取枚举实例
    public static LightEffectEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LightEffectEnum mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }

    // 根据中文描述获取枚举实例
    public static LightEffectEnum getByDesc(String desc) {
        if (desc == null) {
            return null;
        }
        for (LightEffectEnum mode : values()) {
            if (mode.desc.equals(desc)) {
                return mode;
            }
        }
        return null;
    }

    // 获取所有中文描述（用于UI展示，如下拉列表）
    public static String[] getAllDescs() {
        String[] descs = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            descs[i] = values()[i].desc;
        }
        return descs;
    }

    // 获取所有英文标识
    public static String[] getAllCodes() {
        String[] codes = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            codes[i] = values()[i].code;
        }
        return codes;
    }

    // 获取英文标识
    public String getCode() {
        return code;
    }

    // 获取中文描述
    public String getDesc() {
        return desc;
    }
}
