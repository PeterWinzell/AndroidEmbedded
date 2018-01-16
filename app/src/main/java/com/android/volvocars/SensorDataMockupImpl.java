package com.android.volvocars;

import java.util.Random;

/**
 * Created by pwinzell on 2017-12-19.
 */

public class SensorDataMockupImpl extends CarSensor implements SensorData {

    @Override
    public int getSpeed() {
        Random r = new Random();
        return r.nextInt(250);
    }

    @Override
    public int getRPM() {
        Random r = new Random();
        return r.nextInt(4000) + 1000;

    }
}
