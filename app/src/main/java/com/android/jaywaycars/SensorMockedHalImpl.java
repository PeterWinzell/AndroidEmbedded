package com.android.jaywaycars;

import android.hardware.automotive.vehicle.V2_0.VehicleProperty;
import android.hardware.automotive.vehicle.V2_0.VehiclePropertyChangeMode;

import com.android.jaywaycars.mockedvehiclehal.ICarImpl;
import com.android.car.CarPowerManagementService;
import com.android.car.SystemInterface;

import android.car.Car;
import android.car.hardware.CarSensorManager;

//import android.car.test.CarTestManager;
//import android.car.test.CarTestManagerBinderWrapper;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.hardware.automotive.vehicle.V2_0.VehiclePropValue;
import android.hardware.automotive.vehicle.V2_0.VehiclePropertyAccess;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import com.android.jaywaycars.mockedvehiclehal.VehiclePropConfigBuilder;
import com.android.jaywaycars.mockedvehiclehal.MockedVehicleHal;
import com.android.jaywaycars.sensortestview.SensorActivity;
import com.android.jaywaycars.sensortestview.SimulatorTcpClient;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static junit.framework.Assert.assertTrue;


/**
 * Created by pwinzell on 2018-01-06.
 */

public class SensorMockedHalImpl extends SensorHalImpl {

    static final long DEFAULT_WAIT_TIMEOUT_MS = 3000;
    static final long SHORT_WAIT_TIMEOUT_MS = 500;

    // private CarSensorManager mCarSensorManager;
    private MockedVehicleHal mMockedVehicleHal;
    private FakeSystemInterface mFakeSystemInterface;
    private ICarImpl mCarImpl;

    private static final IBinder mCarServiceToken = new Binder();;
    private static boolean mRealCarServiceReleased = false;

    private Handler mMainHandler;
    private Semaphore mWaitForMain;
    private Map<VehiclePropConfigBuilder, MockedVehicleHal.VehicleHalPropertyHandler> mHalConfig;

    private SimulatorTcpClient mSimulatorTcpClient;

    public SensorMockedHalImpl(Context context) {
        super(context);
        startEventInjections();

    }

    private void initDataStructures(){

        mRealCarServiceReleased = false;

        mMainHandler = new Handler(Looper.getMainLooper());
        mWaitForMain = new Semaphore(0);
        mHalConfig = new HashMap<>(10);

        mMockedVehicleHal = new MockedVehicleHal();
        mFakeSystemInterface = new FakeSystemInterface();

        addHalProperties();
    }

    protected void setUp() throws Exception{

        initDataStructures();

        Context context = getCarServiceContext();
        mCarImpl = new ICarImpl(context,mMockedVehicleHal,mFakeSystemInterface);

        if (mCarImpl == null) {
            Log.d("SensorMockedHalImpl", " failed to allocate mCarImpl");
            return;
        }

        Log.d("SensorMockedHalImp"," mCarImpl allocated");

        initMockedHal(false);

        mCar = new android.car.Car(context,mCarImpl,null);

        if (mCar == null){
            Log.d("SensorMockedHalImp"," mCar failed to allocated");
            return;
        }

        Log.d("SensorMockedHalImp"," mCar allocated");

        mCarSensorManager = (CarSensorManager) mCar.getCarManager(Car.SENSOR_SERVICE);

        if (mCarSensorManager == null){
            Log.d("SensorMockedHalImp"," mCarSensorManager not found");
            return;
        }

        Log.d("SensorMockedHalImp"," mCarSensorManager found");

        mSpeedListener = new SensorListener();
        mRpmListener = new SensorListener();

        // mCarSensorManager.registerListener(mSensorListener,CarSensorManager.SENSOR_TYPE_RPM,CarSensorManager.SENSOR_TYPE_CAR_SPEED);

        //TODO: check what the fuck this rate is...
        mCarSensorManager.registerListener(mRpmListener, CarSensorManager.SENSOR_TYPE_RPM,1);
        mCarSensorManager.registerListener(mSpeedListener, CarSensorManager.SENSOR_TYPE_CAR_SPEED,1);



        // Is there support for the sensormanager
        assertTrue(mCarSensorManager.isSensorSupported(CarSensorManager.SENSOR_TYPE_CAR_SPEED));
        assertTrue(mCarSensorManager.isSensorSupported(CarSensorManager.SENSOR_TYPE_RPM));
    }


    private  void addHalProperties(){

        addProperty(VehicleProperty.PARKING_BRAKE_ON,
                VehiclePropValueBuilder.newBuilder(VehicleProperty.PARKING_BRAKE_ON).setBooleanValue(false).build());

        addProperty(VehicleProperty.PERF_VEHICLE_SPEED,
                VehiclePropValueBuilder.newBuilder(VehicleProperty.PERF_VEHICLE_SPEED)
                        .addFloatValue(1f)
                        .build());

        addProperty(VehicleProperty.ENGINE_RPM,
                VehiclePropValueBuilder.newBuilder(VehicleProperty.ENGINE_RPM)
                        .addFloatValue(0f)
                        .build());

    }

    protected Context getCarServiceContext() throws PackageManager.NameNotFoundException{
      return SensorActivity.getContext().createPackageContext("android",Context.CONTEXT_IGNORE_SECURITY);
    }

    private void initMockedHal(boolean release) {
        if (release) {
            mCarImpl.release();
        }

        for (Map.Entry<VehiclePropConfigBuilder, MockedVehicleHal.VehicleHalPropertyHandler> entry
                : mHalConfig.entrySet()) {
            mMockedVehicleHal.addProperty(entry.getKey().build(), entry.getValue());
        }

        mHalConfig.clear();
        mCarImpl.init();
    }

    private synchronized static void releaseRealCarService(Context context) throws Exception {
        if (mRealCarServiceReleased) {
            return;  // We just want to release it once.
        }

        mRealCarServiceReleased = true;  // To make sure it was called once.
        Object waitForConnection = new Object();

        android.car.Car car = android.car.Car.createCar(context, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized (waitForConnection) {
                    waitForConnection.notify();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) { }
        });

        car.connect();
        synchronized (waitForConnection) {
            if (!car.isConnected()) {
                waitForConnection.wait(DEFAULT_WAIT_TIMEOUT_MS);
            }
        }

        /*if (car.isConnected()) {
            Log.i(TAG, "Connected to real car service");
            CarTestManagerBinderWrapper binderWrapper =
                    (CarTestManagerBinderWrapper) car.getCarManager(android.car.Car.TEST_SERVICE);
            assertNotNull(binderWrapper);

            CarTestManager mgr = new CarTestManager(binderWrapper.binder);
            mgr.stopCarService(mCarServiceToken);
        }*/
    }

    protected synchronized VehiclePropConfigBuilder addProperty(int propertyId,
                                                                MockedVehicleHal.VehicleHalPropertyHandler propertyHandler) {
        VehiclePropConfigBuilder builder = VehiclePropConfigBuilder.newBuilder(propertyId);
        mHalConfig.put(builder, propertyHandler);
        return builder;
    }

    protected synchronized VehiclePropConfigBuilder addProperty(int propertyId) {
        VehiclePropConfigBuilder builder = VehiclePropConfigBuilder.newBuilder(propertyId);
        mHalConfig.put(builder, new MockedVehicleHal.DefaultPropertyHandler(builder.build(), null));
        return builder;
    }

    protected synchronized VehiclePropConfigBuilder addProperty(int propertyId,
                                                                VehiclePropValue value) {
        VehiclePropConfigBuilder builder = VehiclePropConfigBuilder.newBuilder(propertyId);
        mHalConfig.put(builder, new MockedVehicleHal.DefaultPropertyHandler(builder.build(), value));
        return builder;
    }

    protected synchronized VehiclePropConfigBuilder addStaticProperty(int propertyId,
                                                                      VehiclePropValue value) {
        VehiclePropConfigBuilder builder = VehiclePropConfigBuilder.newBuilder(propertyId)
                .setChangeMode(VehiclePropertyChangeMode.STATIC)
                .setAccess(VehiclePropertyAccess.READ);

        mHalConfig.put(builder, new MockedVehicleHal.StaticPropertyHandler(value));
        return builder;
    }

    protected synchronized android.car.Car getCar() {
        return mCar;
    }

    protected void runOnMain(final Runnable r) {
        mMainHandler.post(r);
    }

    protected void runOnMainSync(final Runnable r) throws Exception {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                r.run();
                mWaitForMain.release();
            }
        });
        mWaitForMain.acquire();
    }

    protected boolean waitForFakeDisplayState(boolean expectedState) throws Exception {
        return mFakeSystemInterface.waitForDisplayState(expectedState, SHORT_WAIT_TIMEOUT_MS);
    }

    private void startEventInjections(){
        Thread eventInjectionThread = new Thread()
        {
            public void run()
            {
                try {

                   new ConnectTask().execute("");
                    // If we don't add a value for the handbrake we will crash if we inject the speed of 0 , due to
                    // a bug in DrivingStatePolicyClass
                    mMockedVehicleHal.injectEvent(VehiclePropValueBuilder.newBuilder(VehicleProperty.PARKING_BRAKE_ON)
                            .setBooleanValue(false)
                            .setTimestamp()
                            .build());

                    while (true) {
                        Random r = new Random();
                        Float speed =  (float) r.nextInt(80);
                        // Log.d("************* mocked vehicle hal ************ ",Float.toString(speed));
                        Float rpm = (float) r.nextInt(9000);

                        mMockedVehicleHal.injectEvent(VehiclePropValueBuilder.newBuilder(VehicleProperty.ENGINE_RPM)
                                .addFloatValue(rpm)
                                .setTimestamp()
                                .build());

                        mMockedVehicleHal.injectEvent(VehiclePropValueBuilder.newBuilder(VehicleProperty.PERF_VEHICLE_SPEED)
                                .addFloatValue(speed)
                                .setTimestamp()
                                .build());

                       //mMockedVehicleHal.debugDump();
                       //Log.d(" SensorMockedVehicleHalImpl"," still running...");
                        yield();
                        sleep(200);
                    }
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        eventInjectionThread.start();

    }

    private static class FakeSystemInterface extends SystemInterface {

        private boolean mDisplayOn = true;
        private final Semaphore mDisplayStateWait = new Semaphore(0);

        @Override
        public synchronized void setDisplayState(boolean on) {
            mDisplayOn = on;
            mDisplayStateWait.release();
        }

        boolean waitForDisplayState(boolean expectedState, long timeoutMs)
                throws Exception {
            if (expectedState == mDisplayOn) {
                return true;
            }
            mDisplayStateWait.tryAcquire(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            return expectedState == mDisplayOn;
        }

        @Override
        public void releaseAllWakeLocks() {
        }

        @Override
        public void shutdown() { }

        @Override
        public void enterDeepSleep(int wakeupTimeSec) { }

        @Override
        public boolean isSystemSupportingDeepSleep() {
            return false;
        }

        @Override
        public boolean isWakeupCausedByTimer() {
            return false;
        }

        @Override
        public void switchToPartialWakeLock() {
        }

        @Override
        public void switchToFullWakeLock() {
        }

        @Override
        public void startDisplayStateMonitoring(CarPowerManagementService carPowerManagementService) {

        }


        @Override
        public void stopDisplayStateMonitoring() {
        }


    }

    private class ConnectTask extends AsyncTask<String, String, SimulatorTcpClient> {

        @Override
        protected SimulatorTcpClient doInBackground(String... message) {
            //we create a TCPClient object and
            mSimulatorTcpClient = new SimulatorTcpClient(new SimulatorTcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    //publishProgress(message);
                    Log.i("Debug","Input message: " + message);
                }
            });

            mSimulatorTcpClient.run();

            return null;
        }
    }
}
