package com.example.smartcoblight;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcoblight.api.PoemApiClient;
import com.example.smartcoblight.databinding.ActivityMainBinding;
import com.example.smartcoblight.enums.LightEffectEnum;
import com.example.smartcoblight.enums.LightTypeEnum;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MqttHelper.MqttConnectionListener,
        ColorPickerView.OnColorSelectedListener,
        SettingsDialog.OnSettingsChangedListener, PoemApiClient.PoemReqListener {

    // UI组件
    private ColorPickerView colorPickerView;
    private Button powerButton;

    private TextView connectionStatusText;

    // 业务逻辑组件
    private MqttHelper mqttHelper;
    private PreferencesManager preferencesManager;
    private HashMap<LightControlView, LightDevice> devices;

    // 状态变量
    private boolean isPowerOn = false;
    private LightControlView[] lightViews;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("老婆专属遥控器");
        initViews();
        initComponents();
        loadSettings();
        setupListeners();

        initReqInfo();
        // 暂时禁用MQTT连接，让应用先能正常启动
        new android.os.Handler().postDelayed(() -> connectMqtt(), 1000);
//        connectionStatusText.setText("MQTT功能已禁用");
        connectionStatusText.setTextColor(getResources().getColor(R.color.text_secondary));
    }

    private void initReqInfo() {
        PoemApiClient poemApiClient = new PoemApiClient(this);
        poemApiClient.req();
    }

    private void initViews() {
        colorPickerView = binding.colorPickerView;
        powerButton = binding.powerButton;
        // 获取冷灯和暖灯控件
        lightViews = new LightControlView[]{binding.coldLightControl, binding.warmLightControl};
        connectionStatusText = binding.connectionStatusText;
    }

    private void initComponents() {
        preferencesManager = new PreferencesManager(this);
        String mqttBrokerUrl = preferencesManager.getMqttBrokerUrl();
        mqttHelper = new MqttHelper(this, mqttBrokerUrl);


        // 设置类型选择器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, LightEffectEnum.getAllDescs());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.coldLightControl.getTypeSpinner().setAdapter(adapter);
        binding.warmLightControl.getTypeSpinner().setAdapter(adapter);
    }

    private void loadSettings() {
        // 加载保存的设置
        isPowerOn = preferencesManager.getStatus();
        updatePowerButton();

        // 高级按钮的显影
        boolean advVisible = preferencesManager.getAdvanceSettingsVisible();
        for (LightControlView lightView : lightViews) {
            lightView.setAdvancedSettingsVisible(advVisible);
        }

        // 初始化设备列表
        devices = new HashMap<>();
        LightDevice coldSetting = preferencesManager.getLightSettings(LightTypeEnum.COLD_LIGHT.getCode());
        LightDevice warmSetting = preferencesManager.getLightSettings(LightTypeEnum.WARM_LIGHT.getCode());
        if (coldSetting != null) {
            devices.put(binding.coldLightControl, coldSetting); // 冷吉尔丹的灯日
        } else {
            devices.put(binding.coldLightControl, new LightDevice(LightTypeEnum.COLD_LIGHT.getCode())); // 冷灯
        }
        if (warmSetting != null) {
            devices.put(binding.warmLightControl, warmSetting); // 暖他马匹的灯草
        } else {
            devices.put(binding.warmLightControl, new LightDevice(LightTypeEnum.WARM_LIGHT.getCode())); // 暖灯
        }

        // 同步先前保存的数据
        loadLightViewSetting();

        // 设置选中颜色
        int selectedColor = preferencesManager.getSelectedColor();
        // colorPickerView.setSelectedColor(selectedColor); // 暂时注释，因为色盘主要用于装饰
    }

    private void setupListeners() {
        // 色盘选择监听
        colorPickerView.setOnColorSelectedListener(this);

        // 开关按钮监听
        powerButton.setOnClickListener(v -> {
            isPowerOn = !isPowerOn;
            updatePowerButton();
            preferencesManager.setStatus(isPowerOn);
            for (LightDevice device : devices.values()) {
                device.setStatus(isPowerOn);
            }
            saveDataAndSend();
        });

        // 高级设置按钮监听
        binding.advancedButton.setOnClickListener(v -> {
            for (LightControlView lightView : lightViews) {
                boolean isVisible = lightView.isAdvancedSettingsVisible();
                preferencesManager.setAdvanceSettingsVisible(!isVisible);
                lightView.setAdvancedSettingsVisible(!isVisible);
            }
        });

        for (LightControlView lightView : lightViews) {
            LightDevice lightDevice = devices.get(lightView);
            assert lightDevice != null;
            // 滑块监听
            lightView.getLightSlider().addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
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

                    lightDevice.setMinDuty(start);
                    lightDevice.setMaxDuty(end);
                    preferencesManager.saveLightSettings(lightDevice.getId(), lightDevice);

                    saveDataAndSend();
                }
            });

            // 速度滑块监听
            lightView.getSpeedSeekBar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        float speed = progress / 10.0f;
                        lightView.getSpeedValueText().setText(String.format("%.1f", speed));
                        lightDevice.setSpeed(speed);

                        saveDataAndSend();
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
            lightView.getPhaseSeekBar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        float phase = progress / 10.0f;
                        lightView.getPhaseValueText().setText(String.format("%.1f", phase));
                        lightDevice.setPhase(phase);

                        saveDataAndSend();
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
            lightView.getTypeSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedType = LightEffectEnum.getAllDescs()[position];
                    lightDevice.setType(selectedType);

                    saveDataAndSend();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

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


    private void updatePowerButton() {
        if (isPowerOn) {
            powerButton.setText("ON");
            powerButton.setBackgroundResource(R.drawable.power_button_background);
        } else {
            powerButton.setText("OFF");
            powerButton.setBackgroundResource(R.drawable.power_button_background_off);
        }
    }


    /**
     * 改变状态、值时应当缓存，并发送到mqtt中
     */
    private void saveDataAndSend() {
        for (LightDevice lightDevice : devices.values()) {
            preferencesManager.saveLightSettings(lightDevice.getId(), lightDevice);
        }

        if (mqttHelper.isConnected()) {
            mqttHelper.sendLightControl(new ArrayList<>(devices.values()));
        }
        Log.d("MainActivity", "设备配置已更新，MQTT已发送");
    }

    private void loadLightViewSetting() {
        for (LightControlView lightView : lightViews) {
            LightDevice lightSettings = devices.get(lightView);

            lightView.getLightSlider().setStepSize(5f); // 步长
            assert lightSettings != null;
            lightView.getLightSlider().setValues(lightSettings.getMinDuty(), lightSettings.getMaxDuty());

            // 加载高级设置
            float speed = lightSettings.getSpeed();
            lightView.getSpeedSeekBar().setProgress((int) (speed * 10)); // 转换为0-50的整数
            lightView.getSpeedValueText().setText(String.format("%.1f", speed));

            float phase = lightSettings.getPhase();
            lightView.getPhaseSeekBar().setProgress((int) (phase * 10)); // 转换为0-100的整数
            lightView.getPhaseValueText().setText(String.format("%.1f", phase));

            // 设置类型选择器
            String savedType = lightSettings.getType();
            for (int i = 0; i < LightEffectEnum.getAllDescs().length; i++) {
                if (LightEffectEnum.getAllDescs()[i].equals(savedType)) {
                    lightView.getTypeSpinner().setSelection(i);
                    break;
                }
            }
        }
        Log.d("MainActivity", "设备设置已同步！");
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
        Toast.makeText(this, "设置已保存，请重启应用以应用新设置", Toast.LENGTH_LONG).show();
        preferencesManager.saveMqttBrokerUrl(mqttBrokerUrl);
        mqttHelper.setMqttBrokerUrl(mqttBrokerUrl);
        mqttHelper.disconnect();
        mqttHelper.connect(this);
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

    @Override
    public void onPoemFailure() {

    }

    @Override
    public void onPoemSuccess(String content) {
        // 成功回调（子线程），更新UI必须切换到主线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 这里才能安全地执行UI操作
                binding.poemDisplayView.setText(content);
                // 在 onPoemSuccess 的 runOnUiThread 中添加动画
                binding.poemDisplayView.setVisibility(View.VISIBLE);
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(300);
                binding.poemDisplayView.startAnimation(fadeIn);
            }
        });
    }
}