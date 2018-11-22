package com.reactnative.samsunghealth;

import android.database.Cursor;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BloodGlucoseResultListener implements
    HealthResultHolder.ResultListener<ReadResult>
{
    private static final String REACT_MODULE = "RNSamsungHealth";

    private Callback mSuccessCallback;
    private Callback mErrorCallback;
    private SamsungHealthModule mModule;

    public BloodGlucoseResultListener(SamsungHealthModule module, Callback error, Callback success) {
        mSuccessCallback = success;
        mErrorCallback = error;
        mModule = module;
    }

    private WritableMap getDeviceInfo(String uuid) {
        WritableMap map = Arguments.createMap();
        HealthDeviceManager deviceManager = new HealthDeviceManager(mModule.getStore());
        HealthDevice device = deviceManager.getDeviceByUuid(uuid);

        String deviceName = device == null ? null : device.getCustomName();
        String deviceManufacturer = device == null ? null : device.getManufacturer();
        String deviceModel = device == null ? null : device.getModel();
        Integer deviceGroup = device == null ? HealthDevice.GROUP_UNKNOWN : device.getGroup();

        String groupName = "";

        if (deviceName == null) {
            deviceName = "";
        }

        if (deviceManufacturer == null) {
            deviceManufacturer = "";
        }

        if (deviceModel == null) {
            deviceModel = "";
        }

        switch(deviceGroup){
            case HealthDevice.GROUP_MOBILE:
                groupName = "mobileDevice";
                break;
            case HealthDevice.GROUP_EXTERNAL:
                groupName = "peripheral";
                break;
            case HealthDevice.GROUP_COMPANION:
                groupName = "wearable";
                break;
            case HealthDevice.GROUP_UNKNOWN:
                groupName = "unknown";
                break;
        }

        Log.d(REACT_MODULE, "Device: " + uuid + " Name: " + deviceName + " Model: " + deviceModel + " Group: " + groupName);

        map.putString("name", deviceName);
        map.putString("manufacturer", deviceManufacturer);
        map.putString("model", deviceModel);
        map.putString("group", groupName);
        map.putString("uuid", uuid);
        return map;
    }

    @Override
    public void onResult(ReadResult result) {
        Map<String, WritableArray> devices = new HashMap<>();

        Cursor c = null;

        try {
            c = result.getResultCursor();

            Log.d(REACT_MODULE, "Column Names" + Arrays.toString(c.getColumnNames()));

            if (c != null) {
                if (c.getCount() > 0) {
                    byte[] dataText = null;
                    long r = 0;
                    int col;
                    while (c.moveToNext()) {
                        String uuid = c.getString(c.getColumnIndex(HealthConstants.BloodGlucose.DEVICE_UUID));
                        if (!devices.containsKey(uuid)) {
                            devices.put(uuid, Arguments.createArray());
                        }
                        Log.d(REACT_MODULE, "UUID: " + uuid);
                        WritableArray resultSet = devices.get(uuid);

                        WritableMap map = Arguments.createMap();
                        col = c.getColumnIndex(HealthConstants.BloodGlucose.START_TIME);
                        if (col > -1) {
                            map.putDouble(HealthConstants.BloodGlucose.START_TIME, (double) c.getLong(col));
                        }

                        col = c.getColumnIndex(HealthConstants.BloodGlucose.GLUCOSE);
                        if (col > -1) {
                            map.putDouble(HealthConstants.BloodGlucose.GLUCOSE, (double) c.getFloat(col));
                        }

                        /*
                        col = c.getColumnIndex(HealthConstants.HeartRate.START_TIME);
                        if (col > -1) {
                            map.putDouble(HealthConstants.HeartRate.START_TIME, (double) c.getLong(col));
                        }

                        col = c.getColumnIndex(HealthConstants.HeartRate.END_TIME);
                        if (col > -1) {
                            map.putDouble(HealthConstants.HeartRate.END_TIME, (double) c.getLong(col));
                        }

                        col = c.getColumnIndex(HealthConstants.HeartRate.HEART_RATE);
                        if (col > -1) {
                            map.putInt(HealthConstants.HeartRate.HEART_RATE, c.getInt(col));
                        }
                        */
                        resultSet.pushMap(map);
                        r++;
                    }

                    Log.d(REACT_MODULE, "Found rows " + Long.toString(r));
                } else {
                    Log.d(REACT_MODULE, "The cursor count is zero.");
                }
            } else {
                Log.d(REACT_MODULE, "The cursor is null.");
            }
        }
        catch(Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            mErrorCallback.invoke(e.getClass().getName() + " - " + e.getMessage());
        }
        finally {
            if (c != null) {
                c.close();
            }
        }

        WritableArray results = Arguments.createArray();
        for(Map.Entry<String, WritableArray> entry: devices.entrySet()) {
            WritableMap map = Arguments.createMap();
            map.putMap("source", getDeviceInfo(entry.getKey()));
            map.putArray("bloodGlucose", entry.getValue());
            results.pushMap(map);
        }

        Log.d(REACT_MODULE, "Blood Glucose Results");
        mSuccessCallback.invoke(results);
    }
}

/* vim :set ts=4 sw=4 sts=4 et : */
