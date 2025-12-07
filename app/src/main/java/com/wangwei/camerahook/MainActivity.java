package com.wangwei.camerahook;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HookConfig hookConfig = new HookConfig();

        if (!isModuleActive()){
            Toast.makeText(this, "模块未启动", LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "模块已启动", LENGTH_LONG).show();
        }

        TextView tv = findViewById(R.id.tv);
        tv.setText(getMessageInfo("w", "w"));

        TextView configInfo = findViewById(R.id.config_info);
        String readable = hookConfig.isPrefsReadable() ? getString(R.string.prefs_loaded) : getString(R.string.prefs_not_loaded);
        configInfo.setText(getString(
                R.string.config_summary,
                hookConfig.getTargetPackage(),
                hookConfig.getCameraControllerClass(),
                hookConfig.getTriggerMethodName(),
                hookConfig.getCameraFieldName(),
                hookConfig.getVideoPath(),
                readable
        ));
    }

    private boolean isModuleActive(){
        return false;
    }

    private String getMessageInfo(String str1, String str2) {
        String str = str1 + "," + str2;
        return str;
    }
}
