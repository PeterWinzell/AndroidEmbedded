package com.android.jaywaycars;

import android.car.Car;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.CarSensorEvent.CarSpeedData;
import android.car.hardware.CarSensorEvent.RpmData;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by pwinzell on 2017-12-19.
 */

public class SensorHalImpl extends CarSensor implements SensorData {

    protected Car mCar;
    protected CarSensorManager mCarSensorManager;
    protected final DefaultServiceConnectionListener mConnectionListener =
            new DefaultServiceConnectionListener();
    protected SensorListener mRpmListener;
    protected SensorListener mSpeedListener;

    protected int mSpeed,mRpm;

    public  SensorHalImpl(Context context){
        super(context);
    }

    protected void setUp() throws Exception {

        mCar = Car.createCar(mContext.getApplicationContext(), mConnectionListener);
        mCarSensorManager = (CarSensorManager) mCar.getCarManager(Car.SENSOR_SERVICE);

        mRpmListener = new SensorListener();
        mSpeedListener = new SensorListener();

        mCarSensorManager.registerListener(mRpmListener,CarSensorManager.SENSOR_TYPE_RPM,
                5);
        mCarSensorManager.registerListener(mSpeedListener,CarSensorManager.SENSOR_TYPE_CAR_SPEED,
                5);

        if (mCarSensorManager == null)
            throw new Exception(" Car Sensor service unavailable ");
        if ((mCarSensorManager.isSensorSupported(CarSensorManager.SENSOR_TYPE_CAR_SPEED) &&
                mCarSensorManager.isSensorSupported(CarSensorManager.SENSOR_TYPE_RPM)) == false)
            throw new Exception("Speed and/or rpm sensor not supported ");
    }

    @Override
    public int getSpeed(){
        return mSpeed;
    }

    @Override
    public int getRPM() {
        return mRpm;
    }

    protected class SensorListener implements CarSensorManager.OnSensorChangedListener{
        private final Object mSync = new Object();


        @Override
        public void onSensorChanged(CarSensorEvent carSensorEvent) {
            synchronized (mSync){
                if (carSensorEvent.sensorType == CarSensorManager.SENSOR_TYPE_CAR_SPEED){
                    CarSpeedData data = null;
                    data = carSensorEvent.getCarSpeedData(data);
                    if (data != null) {
                        mSpeed = (int) Math.round(3.6 * data.carSpeed);
                    }
                    else
                        mSpeed = 0;
                }
                else if(carSensorEvent.sensorType == CarSensorManager.SENSOR_TYPE_RPM){
                    RpmData data = null;
                    data = carSensorEvent.getRpmData(data);
                    if (data != null)
                        mRpm = (int )Math.round(data.rpm);
                    else
                        mRpm = 0;
                }
            }
        }
    }


    protected class DefaultServiceConnectionListener implements ServiceConnection {
        private final Semaphore mConnectionWait = new Semaphore(0);

        public void waitForConnection(long timeoutMs) throws InterruptedException {
            mConnectionWait.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //assertMainThread();
            Log.d(" SensorHalImp"," service is disconnected ");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // assertMainThread();
            Log.d("SensorHalImp", " service connected ");
            mConnectionWait.release();
        }
    }




}
