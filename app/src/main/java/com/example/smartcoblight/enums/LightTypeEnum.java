package com.example.smartcoblight.enums;

public enum LightTypeEnum {
    COLD_LIGHT(1),
    WARM_LIGHT(2);

    private final int code;
    
    LightTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
