package com.android.jaywaycars;

import android.content.Context;

import com.android.jaywaycars.sensortestview.SensorActivity;

/**
 * Created by pwinzell on 2017-12-19.
 */

public class SensorController {
    private SensorModel sensors;
    private Context appCtx;

    public SensorController(Context app_context/*, CarSensorEvent event*/){
        appCtx = app_context;
        sensors = new SensorModel(app_context);
        // this.event = event;
        //startDataFlow();
    }

    public void startDataFlow(){
        Thread speedUpdate = new Thread()
        {
            public void run()
            {
                try {

                    while (true) {
                        ((SensorActivity) appCtx).updateSpeed(sensors.getSpeed());
                        sleep(100);
                    }
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        Thread rpmUpdate = new Thread(){
            public void run(){
                try {
                    while (true) {
                        ((SensorActivity) appCtx).updateRPM(sensors.getRpm());
                        sleep(50);
                    }
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        speedUpdate.start();
        rpmUpdate.start();


    }
}
