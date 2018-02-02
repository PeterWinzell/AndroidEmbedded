package com.android.jaywaycars.sensortestview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
//import android.widget.TextView;

import com.android.volvocars.drawsomestuff.SinGauge;
import com.android.volvocars.drawsomestuff.TextView;

import com.android.jaywaycars.SensorController;



public class SensorActivity extends AppCompatActivity {

    private TextView speedOutputView;
    private SinGauge rpmOutputView;
    private SensorController controller;

    private static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_sensor);
        setContentView(R.layout.lin_layout);
        speedOutputView = (TextView) findViewById(R.id.textSview);
        rpmOutputView = (SinGauge) findViewById(R.id.sinGview);
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
        speedOutputView.setValue(speed);
    }

    public void updateRPM(int rpm){
        rpmOutputView.setValue(rpm);
    }
}
