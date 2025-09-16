package com.example.smartcoblight;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MqttHelper.MqttConnectionListener,
        ColorPickerView.OnColorSelectedListener,
        SettingsDialog.OnSettingsChangedListener {

    // UI组件
    private ColorPickerView colorPickerView;
    private Button powerButton;
    private Button advancedButton;
    private CardView advancedSettingsCard;
    private RangeSlider coldDutySlider;
    private RangeSlider warmDutySlider;
    private SeekBar speedSeekBar;
    private SeekBar phaseSeekBar;
    private TextView speedValueText;
    private TextView phaseValueText;
    private Spinner typeSpinner;
    private TextView connectionStatusText;

    // 业务逻辑组件
    private MqttHelper mqttHelper;
    private PreferencesManager preferencesManager;
    private List<LightDevice> devices;

    // 状态变量
    private boolean isPowerOn = false;
    private boolean isAdvancedSettingsVisible = false;
    // 状态变量
    private boolean isColdAdvancedVisible = false;
    private boolean isWarmAdvancedVisible = false;
    private LinearLayout coldLightAdvancedSettings;
    private LinearLayout warmLightAdvancedSettings;

    private SeekBar coldSpeedSeekBar;
    private SeekBar warmSpeedSeekBar;
    private SeekBar coldPhaseSeekBar;
    private SeekBar warmPhaseSeekBar;
    private TextView coldSpeedValueText;
    private TextView warmSpeedValueText;
    private TextView coldPhaseValueText;
    private TextView warmPhaseValueText;
    private Spinner coldTypeSpinner;
    private Spinner warmTypeSpinner;
//    private ActivityMainBinding binding;

    // 灯光类型选项
    private String[] lightTypes = {"wave", "breath", "fire", "fade"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("此情无计可消除,才下眉头,却上心头");
        initViews();
        initComponents();
        loadSettings();
        setupListeners();
        // 暂时禁用MQTT连接，让应用先能正常启动
        new android.os.Handler().postDelayed(() -> connectMqtt(), 1000);
//        connectionStatusText.setText("MQTT功能已禁用");
        connectionStatusText.setTextColor(getResources().getColor(R.color.text_secondary));
    }

    private void initViews() {
        colorPickerView = findViewById(R.id.colorPickerView);
        powerButton = findViewById(R.id.powerButton);
        advancedButton = findViewById(R.id.advancedButton);
//        coldLightAdvancedSettings = findViewById(R.id.coldLightAdvancedSettings);
//        warmLightAdvancedSettings = findViewById(R.id.warmLightAdvancedSettings);
//
//        coldDutySlider = findViewById(R.id.coldLightSlider);
//        warmDutySlider = findViewById(R.id.warmLightSlider);
//
////        speedSeekBar = findViewById(R.id.speedSeekBar);
//        coldSpeedSeekBar = findViewById(R.id.coldSpeedSeekBar);
//        warmSpeedSeekBar = findViewById(R.id.warmSpeedSeekBar);
//
//        coldPhaseSeekBar = findViewById(R.id.coldPhaseSeekBar);
//        warmPhaseSeekBar = findViewById(R.id.warmPhaseSeekBar);
//
////        phaseSeekBar = findViewById(R.id.phaseSeekBar);
//        coldSpeedValueText = findViewById(R.id.coldSpeedValueText);
//        warmSpeedValueText = findViewById(R.id.warmSpeedValueText);
//        coldPhaseValueText = findViewById(R.id.coldPhaseValueText);
//        warmPhaseValueText = findViewById(R.id.warmPhaseValueText);
//        coldTypeSpinner = findViewById(R.id.coldTypeSpinner);
//        warmTypeSpinner = findViewById(R.id.warmTypeSpinner);
//        speedValueText = findViewById(R.id.speedValueText);
//        phaseValueText = findViewById(R.id.phaseValueText);
//        typeSpinner = findViewById(R.id.typeSpinner);
        connectionStatusText = findViewById(R.id.connectionStatusText);
    }

    private void initComponents() {
        preferencesManager = new PreferencesManager(this);
        String mqttBrokerUrl = preferencesManager.getMqttBrokerUrl();
        mqttHelper = new MqttHelper(this, mqttBrokerUrl);

        // 初始化设备列表
        devices = new ArrayList<>();
        devices.add(new LightDevice(1)); // 冷灯
        devices.add(new LightDevice(2)); // 暖灯

        // 设置类型选择器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, lightTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coldTypeSpinner.setAdapter(adapter);
        warmTypeSpinner.setAdapter(adapter);
    }

    private void loadSettings() {
        // 加载保存的设置
        isPowerOn = preferencesManager.getStatus();
        updatePowerButton();

        // 加载滑块设置
        coldDutySlider.setStepSize(5f); // 步长
        coldDutySlider.setValues(
                (float) preferencesManager.getColdLightMin(),
                (float) preferencesManager.getColdLightMax()
        );
        warmDutySlider.setStepSize(5f); // 步长
        warmDutySlider.setValues(
                (float) preferencesManager.getWarmLightMin(),
                (float) preferencesManager.getWarmLightMax()
        );

        // 加载高级设置
        float speed = preferencesManager.getSpeed();
        warmSpeedSeekBar.setProgress((int) (speed * 10)); // 转换为0-50的整数
        warmSpeedValueText.setText(String.format("%.1f", speed));
        coldSpeedSeekBar.setProgress((int) (speed * 10)); // 转换为0-50的整数
        coldSpeedValueText.setText(String.format("%.1f", speed));

        float phase = preferencesManager.getPhase();
        coldPhaseSeekBar.setProgress((int) (phase * 10)); // 转换为0-100的整数
        coldPhaseValueText.setText(String.format("%.1f", phase));
        warmPhaseSeekBar.setProgress((int) (phase * 10)); // 转换为0-100的整数
        warmPhaseValueText.setText(String.format("%.1f", phase));

        // 设置类型选择器
        String savedType = preferencesManager.getType();
        for (int i = 0; i < lightTypes.length; i++) {
            if (lightTypes[i].equals(savedType)) {
                coldTypeSpinner.setSelection(i);
                warmTypeSpinner.setSelection(i);
                break;
            }
        }

        // 设置选中颜色
        int selectedColor = preferencesManager.getSelectedColor();
        // colorPickerView.setSelectedColor(selectedColor); // 暂时注释，因为色盘主要用于装饰
    }

    private void setupListeners() {
        // 色盘选择监听
        colorPickerView.setOnColorSelectedListener(this);

        // 开关按钮监听
        powerButton.setOnClickListener(v -> togglePower());

        // 高级设置按钮监听
        advancedButton.setOnClickListener(v -> {
            isColdAdvancedVisible = !isColdAdvancedVisible;
            coldLightAdvancedSettings.setVisibility(isColdAdvancedVisible ? View.VISIBLE : View.GONE);
            isWarmAdvancedVisible = !isWarmAdvancedVisible;
            warmLightAdvancedSettings.setVisibility(isWarmAdvancedVisible ? View.VISIBLE : View.GONE);
        });
        // 滑块监听
        coldDutySlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(RangeSlider slider) {
                // 用户开始滑动
            }

            @Override
            public void onStopTrackingTouch(RangeSlider slider) {
                // 用户停止滑动
                List<Float> values = slider.getValues();
                float start = values.get(0);
                float end = values.get(1);
                Log.d("RangeSlider", "最终滑块范围: " + start + " - " + end);
                preferencesManager.saveColdLightRange((int) start, (int) end);
                updateDevicesAndSend();
            }
        });
        warmDutySlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(RangeSlider slider) {
                // 用户开始滑动
            }

            @Override
            public void onStopTrackingTouch(RangeSlider slider) {
                // 用户停止滑动
                List<Float> values = slider.getValues();
                float start = values.get(0);
                float end = values.get(1);
                Log.d("RangeSlider", "最终滑块范围: " + start + " - " + end);
                preferencesManager.saveWarmLightRange((int) start, (int) end);
                updateDevicesAndSend();
            }
        });

        // 速度滑块监听
        warmSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = progress / 10.0f;
                    speedValueText.setText(String.format("%.1f", speed));
                    preferencesManager.saveSpeed(speed);
                    updateDevicesAndSend();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 速度滑块监听
        coldSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = progress / 10.0f;
                    speedValueText.setText(String.format("%.1f", speed));
                    preferencesManager.saveSpeed(speed);
                    updateDevicesAndSend();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 相位滑块监听
        coldPhaseSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float phase = progress / 10.0f;
                    phaseValueText.setText(String.format("%.1f", phase));
                    preferencesManager.savePhase(phase);
                    updateDevicesAndSend();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        warmPhaseSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float phase = progress / 10.0f;
                    phaseValueText.setText(String.format("%.1f", phase));
                    preferencesManager.savePhase(phase);
                    updateDevicesAndSend();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 类型选择监听
        coldTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = lightTypes[position];
                preferencesManager.saveType(selectedType);
                updateDevicesAndSend();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 类型选择监听
        warmTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = lightTypes[position];
                preferencesManager.saveType(selectedType);
                updateDevicesAndSend();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // 长按连接状态显示设置
        connectionStatusText.setOnLongClickListener(v -> {
            showSettingsDialog();
            return true;
        });
    }

    private void connectMqtt() {
        mqttHelper.setConnectionListener(this);
        mqttHelper.connect(this);
    }

    private void showSettingsDialog() {
        String currentMqttBroker = preferencesManager.getMqttBrokerUrl();
        SettingsDialog dialog = SettingsDialog.newInstance(currentMqttBroker);
        dialog.show(getSupportFragmentManager(), "settings_dialog");
    }

    private void togglePower() {
        isPowerOn = !isPowerOn;
        updatePowerButton();
        preferencesManager.saveStatus(isPowerOn);
        updateDevicesAndSend();
    }

    private void updatePowerButton() {
        if (isPowerOn) {
            powerButton.setText("ON");
            powerButton.setBackgroundResource(R.drawable.power_button_background);
        } else {
            powerButton.setText("OFF");
            powerButton.setBackgroundResource(R.drawable.power_button_background_off);
        }
    }

    private void toggleAdvancedSettings() {
        isAdvancedSettingsVisible = !isAdvancedSettingsVisible;
        advancedSettingsCard.setVisibility(isAdvancedSettingsVisible ? View.VISIBLE : View.GONE);
    }

    private void updateDevicesAndSend() {
        // 更新设备配置
        LightDevice coldLight = devices.get(0);
        coldLight.setStatus(isPowerOn);
        coldLight.setMinDuty(coldDutySlider.getValues().get(0));
        coldLight.setMaxDuty(coldDutySlider.getValues().get(1));
        coldLight.setSpeed(preferencesManager.getSpeed());
        coldLight.setPhase(preferencesManager.getPhase());
        coldLight.setType(preferencesManager.getType());

        LightDevice warmLight = devices.get(1);
        warmLight.setStatus(isPowerOn);
        warmLight.setMinDuty(warmDutySlider.getValues().get(0));
        warmLight.setMaxDuty(warmDutySlider.getValues().get(1));
        warmLight.setSpeed(preferencesManager.getSpeed());
        warmLight.setPhase(preferencesManager.getPhase());
        warmLight.setType(preferencesManager.getType());

        // 暂时禁用MQTT发送
        // if (mqttClient.isConnected()) {
        //     mqttClient.sendLightControl(devices);
        // }
        Log.d("MainActivity", "设备配置已更新，但MQTT发送已禁用");
    }

    // MqttConnectionListener 实现
    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            connectionStatusText.setText("已连接");
            connectionStatusText.setTextColor(getResources().getColor(R.color.secondary_color));
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            connectionStatusText.setText("未连接");
            connectionStatusText.setTextColor(getResources().getColor(R.color.error_color));
        });
    }

    @Override
    public void onConnectionFailed(String reason) {
        runOnUiThread(() -> {
            connectionStatusText.setText("连接失败: " + reason);
            connectionStatusText.setTextColor(getResources().getColor(R.color.error_color));
            Toast.makeText(this, "MQTT连接失败: " + reason, Toast.LENGTH_LONG).show();
        });
    }

    // ColorPickerView.OnColorSelectedListener 实现
    @Override
    public void onColorSelected(int color) {
        preferencesManager.saveSelectedColor(color);
        // 注意：色盘选择仅用于装饰，不发送到设备
    }

    // SettingsDialog.OnSettingsChangedListener 实现
    @Override
    public void onSettingsChanged(String mqttBrokerUrl) {
        preferencesManager.saveMqttBrokerUrl(mqttBrokerUrl);
        Toast.makeText(this, "设置已保存，请重启应用以应用新设置", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttHelper != null) {
            mqttHelper.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 确保MQTT始终连着
        if (mqttHelper != null) {
            mqttHelper.connect(this);
        }
    }
}