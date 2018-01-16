package com.android.volvocars;

import android.content.Context;

/**
 * Created by pwinzell on 2017-12-19.
 */

public class SensorModel {
    private  int speed;
    private  int rpm;
    private  Context appCtx;
    private SensorData sensorData;

    public SensorModel(final Context ctx){
        appCtx = ctx;
        //@TODO retrieve the sensor implementation from a preference setting here. Right now it is hardcoded
        sensorData = (SensorData) SensorDataImplementationFactory.getSensorDataImplementation(SensorImplementations.MOCKED_HAL_LAYER);
    }

    public int getSpeed(){
        speed = sensorData.getSpeed();
        return speed;
    }

    public int getRpm(){
        rpm = sensorData.getRPM();
        return rpm;
    }
}
