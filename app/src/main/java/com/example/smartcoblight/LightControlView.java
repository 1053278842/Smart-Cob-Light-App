package com.example.smartcoblight;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.RangeSlider;

public class LightControlView extends CardView {

    private TextView lightTitle;
    private RangeSlider lightSlider;
    private SeekBar lightSpeedSeekBar;
    private SeekBar lightPhaseSeekBar;
    private Spinner lightTypeSpinner;
    private TextView lightSpeedValueText;
    private TextView lightPhaseValueText;
    private LinearLayout lightAdvancedSettings;

    public LightControlView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public LightControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LightControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // 初始化布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.light_control_view, this, true);

        // 绑定视图
        lightTitle = view.findViewById(R.id.lightTitle);
        lightSlider = view.findViewById(R.id.lightSlider);
        lightSpeedSeekBar = view.findViewById(R.id.lightSpeedSeekBar);
        lightPhaseSeekBar = view.findViewById(R.id.lightPhaseSeekBar);
        lightTypeSpinner = view.findViewById(R.id.lightTypeSpinner);
        lightSpeedValueText = view.findViewById(R.id.lightSpeedValueText);
        lightPhaseValueText = view.findViewById(R.id.lightPhaseValueText);
        lightAdvancedSettings = view.findViewById(R.id.lightAdvancedSettings);

        // 处理自定义属性
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.LightControlView,
                    0,
                    0
            );

            try {
                String lightName = typedArray.getString(R.styleable.LightControlView_lightName);
                int lightId = typedArray.getInt(R.styleable.LightControlView_lightId, 0);
                int lightColor = typedArray.getColor(
                        R.styleable.LightControlView_lightColor,
                        ContextCompat.getColor(context, android.R.color.black)
                );
                boolean showAdvanced = typedArray.getBoolean(
                        R.styleable.LightControlView_showAdvancedSettings,
                        false
                );

                // 设置视图属性
                setLightInfo(lightName, lightId, lightColor);
                setAdvancedSettingsVisible(showAdvanced);
            } finally {
                typedArray.recycle();
            }
        }

        // 设置监听器
        setupListeners();
    }

    public void setLightInfo(String name, int id, int color) {
        if (name != null) {
            lightTitle.setText(name);
            lightTitle.setTextColor(color);
        }
    }


    public boolean isAdvancedSettingsVisible() {
        return lightAdvancedSettings.getVisibility() == View.VISIBLE;
    }

    public void setAdvancedSettingsVisible(boolean visible) {
        lightAdvancedSettings.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public RangeSlider getLightSlider() {
        return lightSlider;
    }

    public SeekBar getSpeedSeekBar() {
        return lightSpeedSeekBar;
    }

    public SeekBar getPhaseSeekBar() {
        return lightPhaseSeekBar;
    }

    public Spinner getTypeSpinner() {
        return lightTypeSpinner;
    }

    public TextView getSpeedValueText() {
        return lightSpeedValueText;
    }

    public TextView getPhaseValueText() {
        return lightPhaseValueText;
    }

    private void setupListeners() {
        // 速度滑块监听器
        lightSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speedValue = progress / 10.0f;
                lightSpeedValueText.setText(String.format("%.1f", speedValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }
        });

        // 相位滑块监听器
        lightPhaseSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float phaseValue = progress / 10.0f;
                lightPhaseValueText.setText(String.format("%.1f", phaseValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }
        });
    }
}