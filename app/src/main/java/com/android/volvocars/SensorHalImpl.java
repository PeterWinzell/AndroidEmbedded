package com.android.volvocars;

import android.app.Application;

import android.car.Car;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.CarSensorEvent.CarSpeedData;
import android.car.hardware.CarSensorEvent.RpmData;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

import android.hardware.SensorEventListener;
import android.os.IBinder;
import android.os.Looper;
import android.test.AndroidTestCase;
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
    protected SensorListener mSensorListener;

    protected int mSpeed,mRpm;

    public  SensorHalImpl(Context context){
        super(context);
    }

    protected void setUp() throws Exception {

        mCar = Car.createCar(mContext.getApplicationContext(), mConnectionListener);
        mCarSensorManager = (CarSensorManager) mCar.getCarManager(Car.SENSOR_SERVICE);
        mSensorListener = new SensorListener();
        mCarSensorManager.registerListener(mSensorListener,CarSensorManager.SENSOR_TYPE_RPM,
                CarSensorManager.SENSOR_TYPE_CAR_SPEED);

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
                    carSensorEvent.getCarSpeedData(data);
                    if (data != null) {
                        mSpeed = (int) Math.round(3.6 * data.carSpeed);
                    }
                }
                else if(carSensorEvent.sensorType == CarSensorManager.SENSOR_TYPE_RPM){
                    RpmData data = null;
                    carSensorEvent.getRpmData(data);
                    mRpm = (int )Math.round(data.rpm);
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
