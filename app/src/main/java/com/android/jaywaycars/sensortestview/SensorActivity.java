package com.android.jaywaycars.sensortestview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.jaywaycars.SensorController;



public class SensorActivity extends AppCompatActivity {

    private TextView speedOutputView;
    private TextView rpmOutputView;
    private SensorController controller;

    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        speedOutputView = (TextView) findViewById(R.id.speedOut);
        rpmOutputView = (TextView) findViewById(R.id.rpmOut);
       // mspeedRpmView = (View) findViewById(R.id.spee)

        setContext(getApplicationContext());

        controller = new SensorController(this);

    }

    public static Context getContext(){
        return mContext;
    }

    public static void setContext(Context mCtx){
        mContext =  mCtx;
    }

    public void updateSpeed(int speed){
        speedOutputView.setText(Integer.toString(speed));
    }

    public void updateRPM(int rpm){
        rpmOutputView.setText(Integer.toString(rpm));
    }
}
