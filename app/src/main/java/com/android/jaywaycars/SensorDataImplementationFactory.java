package com.android.jaywaycars;

import com.android.jaywaycars.sensortestview.SensorActivity;

/**
 * Created by pwinzell on 2017-12-19.
 */

public class SensorDataImplementationFactory {

    public static SensorData getSensorDataImplementation(SensorImplementations imp){
        switch (imp){
            case JAVA_LAYER_MOCKUP:
                return  new SensorDataMockupImpl();
            case HAL_LAYER:
                return new SensorHalImpl(SensorActivity.getContext());
            case MOCKED_HAL_LAYER:
                return new SensorMockedHalImpl(SensorActivity.getContext());
            default:
                return null; // throw exception here ?
        }
    }
}
