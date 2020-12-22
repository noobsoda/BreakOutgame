package com.example.breakoutgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements SensorEventListener /*AppCompatActivity*/ {
    BreakOutEngine breakOutEngine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        breakOutEngine = new BreakOutEngine(this, size.x, size.y);
        setContentView(breakOutEngine);

    }
    @Override
    protected void onResume() {
        super.onResume();


        breakOutEngine.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();

        breakOutEngine.pause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
