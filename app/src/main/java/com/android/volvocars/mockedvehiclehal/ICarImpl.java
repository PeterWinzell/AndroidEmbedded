package com.android.volvocars.mockedvehiclehal;

/**
 * Created by pwinzell on 2018-01-17.
 */


import android.app.UiModeManager;
import android.car.Car;
import android.car.ICar;
import android.car.annotation.FutureFeature;
import android.car.cluster.renderer.IInstrumentClusterNavigation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.automotive.vehicle.V2_0.IVehicle;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.Trace;
import android.util.Log;

import com.android.car.CarLog;
import com.android.car.CarPowerManagementService;
import com.android.car.CarSensorService;
import com.android.car.CarServiceBase;
import com.android.car.SystemActivityMonitoringService;
import com.android.car.SystemInterface;
import com.android.car.cluster.InstrumentClusterService;
import com.android.car.hal.VehicleHal;
import com.android.car.internal.FeatureConfiguration;
import com.android.car.internal.FeatureUtil;
import com.android.car.pm.CarPackageManagerService;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ICarImpl extends ICar.Stub {

    public static final String INTERNAL_INPUT_SERVICE = "internal_input";
    public static final String INTERNAL_SYSTEM_ACTIVITY_MONITORING_SERVICE =
            "system_activity_monitoring";

    private final Context mContext;
    private final VehicleHal mHal;

    private final CarSensorService mCarSensorService;

    private final CarServiceBase[] mAllServices;

    private static final String TAG = "ICarImpl";
    private static final String VHAL_TIMING_TAG = "VehicleHalTiming";


    public ICarImpl(Context serviceContext, IVehicle vehicle, SystemInterface systemInterface){
        mContext = serviceContext;
        mHal = new VehicleHal(vehicle);

        mCarSensorService = new CarSensorService(serviceContext, mHal.getSensorHal());

        List<CarServiceBase> allServices = new ArrayList<>(Arrays.asList(
                mCarSensorService
        ));

        mAllServices = allServices.toArray(new CarServiceBase[0]);
    }

    public void init() {
        mHal.init();
        Log.d(" ICarImpl"," Vehicle Hal initialized ");

        for (CarServiceBase service : mAllServices) {
            service.init();
        }

        Log.d(" ICarImpl", " Vehicle Services initialized");
    }

    public void release() {
        // release done in opposite order from init
        for (int i = mAllServices.length - 1; i >= 0; i--) {
            mAllServices[i].release();
        }
        mHal.release();
    }

    public void vehicleHalReconnected(IVehicle vehicle) {
        mHal.vehicleHalReconnected(vehicle);
        for (CarServiceBase service : mAllServices) {
            service.vehicleHalReconnected();
        }
    }

    @Override
    public void setCarServiceHelper(IBinder helper) {
        int uid = Binder.getCallingUid();
        /*if (uid != Process.SYSTEM_UID) {
            throw new SecurityException("Only allowed from system");
        }
        synchronized (this) {
            mICarServiceHelper = ICarServiceHelper.Stub.asInterface(helper);
        }*/
    }

    @Override
    public IBinder getCarService(String serviceName) {
        switch (serviceName) {
            case Car.SENSOR_SERVICE:
                return mCarSensorService;
            default:
                Log.w(CarLog.TAG_SERVICE, "getCarService for unknown service:" + serviceName);
                return null;
        }
    }

    @Override
    public int getCarConnectionType() {
        return Car.CONNECTION_TYPE_EMBEDDED;
    }

    public CarServiceBase getCarInternalService(String serviceName) {
        /*switch (serviceName) {
            case INTERNAL_INPUT_SERVICE:
                return mCarInputService;
            case INTERNAL_SYSTEM_ACTIVITY_MONITORING_SERVICE:
                return mSystemActivityMonitoringService;
            default:
                Log.w(CarLog.TAG_SERVICE, "getCarInternalService for unknown service:" +
                        serviceName);
                return null;
        }*/
        return null;
    }

    public static void assertVehicleHalMockPermission(Context context) {
        assertPermission(context, Car.PERMISSION_MOCK_VEHICLE_HAL);
    }

    public static void assertCabinPermission(Context context) {
        assertPermission(context, Car.PERMISSION_CAR_CABIN);
    }

    public static void assertNavigationManagerPermission(Context context) {
        assertPermission(context, Car.PERMISSION_CAR_NAVIGATION_MANAGER);
    }

    public static void assertClusterManagerPermission(Context context) {
        assertPermission(context, Car.PERMISSION_CAR_INSTRUMENT_CLUSTER_CONTROL);
    }

    public static void assertHvacPermission(Context context) {
        assertPermission(context, Car.PERMISSION_CAR_HVAC);
    }

    private static void assertRadioPermission(Context context) {
        assertPermission(context, Car.PERMISSION_CAR_RADIO);
    }

    public static void assertProjectionPermission(Context context) {
        assertPermission(context, Car.PERMISSION_CAR_PROJECTION);
    }

    public static void assertVendorExtensionPermission(Context context) {
        assertPermission(context, Car.PERMISSION_VENDOR_EXTENSION);
    }

    public static void assertAnyDiagnosticPermission(Context context) {
        assertAnyPermission(context,
                Car.PERMISSION_CAR_DIAGNOSTIC_READ_ALL,
                Car.PERMISSION_CAR_DIAGNOSTIC_CLEAR);
    }

    @FutureFeature
    public static void assertVmsPublisherPermission(Context context) {
        assertPermission(context, Car.PERMISSION_VMS_PUBLISHER);
    }

    @FutureFeature
    public static void assertVmsSubscriberPermission(Context context) {
        assertPermission(context, Car.PERMISSION_VMS_SUBSCRIBER);
    }

    public static void assertPermission(Context context, String permission) {
        if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("requires " + permission);
        }
    }

    public static void assertAnyPermission(Context context, String... permissions) {
        for (String permission : permissions) {
            if (context.checkCallingOrSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        throw new SecurityException("requires any of " + Arrays.toString(permissions));
    }

    void dump(PrintWriter writer) {
        writer.println("*FutureConfig, DEFAULT:" + FeatureConfiguration.DEFAULT);
        //TODO dump all feature flags by reflection
        writer.println("*Dump all services*");
        for (CarServiceBase service : mAllServices) {
            service.dump(writer);
        }

       /* if (mCarTestService != null) {
            mCarTestService.dump(writer);
        }*/
        writer.println("*Dump Vehicle HAL*");
        mHal.dump(writer);
    }

    void execShellCmd(String[] args, PrintWriter writer) {
        new CarShellCommand().exec(args, writer);
    }

    private static void traceBegin(String name) {
        //Slog.i(TAG, name);
        //mBootTiming.traceBegin(name);
    }

    private static void traceEnd() {
        //mBootTiming.traceEnd();
    }

    private class CarShellCommand {
        private static final String COMMAND_HELP = "-h";
        private static final String COMMAND_DAY_NIGHT_MODE = "day-night-mode";
        private static final String COMMAND_INJECT_EVENT = "inject-event";

        private static final String PARAM_DAY_MODE = "day";
        private static final String PARAM_NIGHT_MODE = "night";
        private static final String PARAM_SENSOR_MODE = "sensor";
        private static final String PARAM_ZONED_BOOLEAN = "zoned-boolean";
        private static final String PARAM_GLOBAL_INT = "global-integer";

        private void dumpHelp(PrintWriter pw) {
            pw.println("Car service commands:");
            pw.println("\t-h");
            pw.println("\t  Print this help text.");
            pw.println("\tday-night-mode [day|night|sensor]");
            pw.println("\t  Force into day/night mode or restore to auto.");
            pw.println("\tinject-event zoned-boolean propertyType zone [true|false]");
            pw.println("\t  Inject a Boolean HAL Event. ");
        }

        public void exec(String[] args, PrintWriter writer) {
            String arg = args[0];
            switch (arg) {
                case COMMAND_HELP:
                    dumpHelp(writer);
                    break;
                case COMMAND_DAY_NIGHT_MODE:
                    String value = args.length < 1 ? "" : args[1];
                    forceDayNightMode(value, writer);
                    break;
                case COMMAND_INJECT_EVENT:
                    String eventType;
                    if (args.length > 1) {
                        eventType = args[1].toLowerCase();
                        switch (eventType) {
                            case PARAM_ZONED_BOOLEAN:
                                if (args.length < 5) {
                                    writer.println("Incorrect number of arguments.");
                                    dumpHelp(writer);
                                    break;
                                }
                                inject_zoned_boolean_event(args[2], args[3], args[4], writer);
                                break;

                            case PARAM_GLOBAL_INT:
                                if (args.length < 4) {
                                    writer.println("Incorrect number of Arguments");
                                    dumpHelp(writer);
                                    break;
                                }
                                inject_global_integer_event(args[2], args[3], writer);
                                break;

                            default:
                                writer.println("Unsupported event type");
                                dumpHelp(writer);
                                break;
                        }
                    }
                    break;
                default:
                    writer.println("Unknown command.");
                    dumpHelp(writer);
            }
        }

        private void forceDayNightMode(String arg, PrintWriter writer) {
            int mode;
            /*switch (arg) {
                case PARAM_DAY_MODE:
                    mode = CarNightService.FORCED_DAY_MODE;
                    break;
                case PARAM_NIGHT_MODE:
                    mode = CarNightService.FORCED_NIGHT_MODE;
                    break;
                case PARAM_SENSOR_MODE:
                    mode = CarNightService.FORCED_SENSOR_MODE;
                    break;
                default:
                    writer.println("Unknown value. Valid argument: " + PARAM_DAY_MODE + "|"
                            + PARAM_NIGHT_MODE + "|" + PARAM_SENSOR_MODE);
                    return;
            }
            int current = mCarNightService.forceDayNightMode(mode);
            String currentMode = null;
            switch (current) {
                case UiModeManager.MODE_NIGHT_AUTO:
                    currentMode = PARAM_SENSOR_MODE;
                    break;
                case UiModeManager.MODE_NIGHT_YES:
                    currentMode = PARAM_NIGHT_MODE;
                    break;
                case UiModeManager.MODE_NIGHT_NO:
                    currentMode = PARAM_DAY_MODE;
                    break;
            }
            writer.println("DayNightMode changed to: " + currentMode);*/
        }

        /**
         * Inject a fake boolean HAL event to help testing.
         *
         * @param property - Vehicle Property
         * @param value    - boolean value for the property
         * @param writer   - Printwriter
         */
        private void inject_zoned_boolean_event(String property, String zone, String value,
                                                PrintWriter writer) {
            Log.d(CarLog.TAG_SERVICE, "Injecting Boolean event");
            boolean event;
            int propId;
            int zoneId;
            if (value.equalsIgnoreCase("true")) {
                event = true;
            } else {
                event = false;
            }
            try {
                propId = Integer.decode(property);
                zoneId = Integer.decode(zone);
            } catch (NumberFormatException e) {
                writer.println("Invalid property Id or Zone Id. Prefix hex values with 0x");
                return;
            }
            mHal.injectBooleanEvent(propId, zoneId, event);
        }

        /**
         * Inject a fake Integer HAL event to help testing.
         *
         * @param property - Vehicle Property
         * @param value    - Integer value to inject
         * @param writer   - PrintWriter
         */
        private void inject_global_integer_event(String property, String value,
                                                 PrintWriter writer) {
            Log.d(CarLog.TAG_SERVICE, "Injecting integer event");
            int propId;
            int eventValue;
            try {
                propId = Integer.decode(property);
                eventValue = Integer.decode(value);
            } catch (NumberFormatException e) {
                writer.println("Invalid property Id or event value.  Prefix hex values with 0x");
                return;
            }
            mHal.injectIntegerEvent(propId, eventValue);
        }

    }
}