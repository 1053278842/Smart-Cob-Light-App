package com.example.smartcoblight;

import android.content.Context;
import android.util.Log;

import com.example.smartcoblight.enums.LightEffectEnum;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;

public class MqttHelper {
    private static final String TAG = "MqttClient";
    private static final String CLIENT_ID = "SmartCobLight_Android_" + System.currentTimeMillis();
    private static final String TOPIC_CONTROL = "/ll/washroom/light/light001/down/control";

    private MqttAsyncClient mqttClient;
    private Context context;
    private Gson gson;
    private MqttConnectionListener connectionListener;
    private String mqttBrokerUrl;

    public MqttHelper(Context context) {
        new MqttHelper(context);
    }

    public MqttHelper(Context context, String brokerUrl) {
        this.connectionListener = (MqttConnectionListener) context;
        this.context = context;
        this.gson = new Gson();
        this.mqttBrokerUrl = brokerUrl;
        try {
            mqttClient = new MqttAsyncClient(brokerUrl, CLIENT_ID, new MemoryPersistence());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        initMqttClient();
    }

    private void initMqttClient() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "MQTT连接丢失", cause);
                if (connectionListener != null) {
                    connectionListener.onDisconnected();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "收到消息: " + topic + " - " + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "消息发送完成");
            }
        });
    }

    /*连接到Mqtt broker*/
    public void connect(MqttConnectionListener listener) {
        if (mqttBrokerUrl.contains("x") || mqttBrokerUrl.contains("X")) {
            if (connectionListener != null) {
                connectionListener.onConnectionFailed("第一次启动,请长按配置Broker IP");
                return;
            }
        }
        /*已连接就忽略了 开发者模式*/
        if (mqttClient.isConnected()) {
            if (listener != null) {
                listener.onConnected();
            }
            return;
        }

        this.connectionListener = listener;

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "MQTT连接成功");
                    if (connectionListener != null) {
                        connectionListener.onConnected();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT连接失败", exception);
                    if (connectionListener != null) {
                        connectionListener.onConnectionFailed(exception.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "MQTT Connect发生异常", e);
            if (connectionListener != null) {
                connectionListener.onConnectionFailed(e.getMessage());
            }
        }


    }

    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                Log.d(TAG, "MQTT断开连接");
            } catch (MqttException e) {
                Log.e(TAG, "MQTT断开连接失败", e);
            }
        }
    }

    public void sendLightControl(List<LightDevice> devices) {
        if (!mqttClient.isConnected()) {
            Log.w(TAG, "MQTT未连接，无法发送控制命令");
            return;
        }

        for (LightDevice device : devices) {
            device.setType(LightEffectEnum.getByDesc(device.getType()).getCode());
        }
        LightControlRequest request = new LightControlRequest(devices);
        String jsonPayload = gson.toJson(request);
        for (LightDevice device : devices) {
            device.setType(LightEffectEnum.getByCode(device.getType()).getDesc());
        }

        MqttMessage message = new MqttMessage(jsonPayload.getBytes());
        message.setQos(1);
        message.setRetained(true);

        try {
            mqttClient.publish(TOPIC_CONTROL, message);
            Log.d(TAG, "发送灯光控制命令: " + jsonPayload);
        } catch (MqttException e) {
            Log.e(TAG, "发送MQTT消息失败", e);
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public void setMqttBrokerUrl(String mqttBrokerUrl) {
        this.mqttBrokerUrl = mqttBrokerUrl;
    }


    public void setConnectionListener(MqttConnectionListener listener) {
        this.connectionListener = listener;
    }

    public interface MqttConnectionListener {
        void onConnected();

        void onDisconnected();

        void onConnectionFailed(String reason);
    }
}
