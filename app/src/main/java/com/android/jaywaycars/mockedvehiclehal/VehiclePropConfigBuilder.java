package com.android.jaywaycars.mockedvehiclehal;

/**
 * Created by pwinzell on 2018-01-09.
 */

import android.hardware.automotive.vehicle.V2_0.VehicleAreaConfig;
import android.hardware.automotive.vehicle.V2_0.VehiclePropConfig;

import java.util.Collection;
import java.util.Iterator;

public class VehiclePropConfigBuilder {
    private final VehiclePropConfig mConfig = new VehiclePropConfig();

    public static VehiclePropConfigBuilder newBuilder(int propId) {
        return new VehiclePropConfigBuilder(propId);
    }

    private VehiclePropConfigBuilder(int propId) {
        this.mConfig.prop = propId;
        this.mConfig.access = 3;
        this.mConfig.changeMode = 1;
    }

    private VehiclePropConfig clone(VehiclePropConfig propConfig) {
        VehiclePropConfig newConfig = new VehiclePropConfig();
        newConfig.prop = propConfig.prop;
        newConfig.access = propConfig.access;
        newConfig.changeMode = propConfig.changeMode;
        newConfig.supportedAreas = propConfig.supportedAreas;
        newConfig.configFlags = propConfig.configFlags;
        newConfig.configString = propConfig.configString;
        newConfig.minSampleRate = propConfig.minSampleRate;
        newConfig.maxSampleRate = propConfig.maxSampleRate;
        newConfig.configArray.addAll(propConfig.configArray);
        Iterator var3 = propConfig.areaConfigs.iterator();

        while(var3.hasNext()) {
            VehicleAreaConfig area = (VehicleAreaConfig)var3.next();
            VehicleAreaConfig newArea = new VehicleAreaConfig();
            newArea.areaId = area.areaId;
            newArea.minInt32Value = area.minInt32Value;
            newArea.maxInt32Value = area.maxInt32Value;
            newArea.minInt64Value = area.minInt64Value;
            newArea.maxInt64Value = area.maxInt64Value;
            newArea.minFloatValue = area.minFloatValue;
            newArea.maxFloatValue = area.maxFloatValue;
            newConfig.areaConfigs.add(newArea);
        }

        return newConfig;
    }

    public VehiclePropConfigBuilder setAccess(int access) {
        this.mConfig.access = access;
        return this;
    }

    public VehiclePropConfigBuilder setChangeMode(int changeMode) {
        this.mConfig.changeMode = changeMode;
        return this;
    }

    public VehiclePropConfigBuilder setSupportedAreas(int supportedAreas) {
        this.mConfig.supportedAreas = supportedAreas;
        return this;
    }

    public VehiclePropConfigBuilder setConfigFlags(int configFlags) {
        this.mConfig.configFlags = configFlags;
        return this;
    }

    public VehiclePropConfigBuilder setConfigString(String configString) {
        this.mConfig.configString = configString;
        return this;
    }

    public VehiclePropConfigBuilder setConfigArray(Collection<Integer> configArray) {
        this.mConfig.configArray.clear();
        this.mConfig.configArray.addAll(configArray);
        return this;
    }

    public VehiclePropConfigBuilder addAreaConfig(int areaId, int minValue, int maxValue) {
        VehicleAreaConfig area = new VehicleAreaConfig();
        area.areaId = areaId;
        area.minInt32Value = minValue;
        area.maxInt32Value = maxValue;
        this.mConfig.areaConfigs.add(area);
        return this;
    }

    public VehiclePropConfigBuilder addAreaConfig(int areaId, float minValue, float maxValue) {
        VehicleAreaConfig area = new VehicleAreaConfig();
        area.areaId = areaId;
        area.minFloatValue = minValue;
        area.maxFloatValue = maxValue;
        this.mConfig.areaConfigs.add(area);
        return this;
    }

    public VehiclePropConfig build() {
        return this.clone(this.mConfig);
    }
}
