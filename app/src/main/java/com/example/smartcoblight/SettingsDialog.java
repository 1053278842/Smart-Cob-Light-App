package com.example.smartcoblight;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SettingsDialog extends DialogFragment {
    
    private EditText mqttBrokerEditText;
    private OnSettingsChangedListener listener;
    
    public interface OnSettingsChangedListener {
        void onSettingsChanged(String mqttBrokerUrl);
    }
    
    public static SettingsDialog newInstance(String currentMqttBroker) {
        SettingsDialog dialog = new SettingsDialog();
        Bundle args = new Bundle();
        args.putString("mqtt_broker", currentMqttBroker);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsChangedListener) {
            listener = (OnSettingsChangedListener) context;
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings, null);
        
        mqttBrokerEditText = view.findViewById(R.id.mqttBrokerEditText);
        
        // 设置当前值
        String currentMqttBroker = getArguments().getString("mqtt_broker", "tcp://121.36.251.16:1883");
        mqttBrokerEditText.setText(currentMqttBroker);
        
        builder.setView(view)
                .setTitle("设置")
                .setPositiveButton("保存", (dialog, id) -> {
                    if (listener != null) {
                        String newMqttBroker = mqttBrokerEditText.getText().toString().trim();
                        listener.onSettingsChanged(newMqttBroker);
                    }
                })
                .setNegativeButton("取消", (dialog, id) -> {
                    // 用户取消
                });
        
        return builder.create();
    }
}
