package com.android.jaywaycars;

import android.content.Context;
import android.util.Log;

/**
 * Created by pwinzell on 2017-12-19.
 */

public class CarSensor {

    protected Context  mContext;

    public CarSensor(){

    }

    public  CarSensor(Context context){
        mContext = context;

        try {
            setUp();
        }
        catch (Exception ex){
            Log.d(" CarSensor ", ex.toString());
        }
    }

    protected void setUp() throws Exception {

    }

}
