package com.reactnative.samsunghealth;

import android.database.Cursor;
import android.util.Log;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.LifecycleEventListener;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.InsertRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by firodj on 5/2/17.
 */

@ReactModule(name = "RNSamsungHealth")
public class SamsungHealthModule extends ReactContextBaseJavaModule implements
        LifecycleEventListener {

    private static final String REACT_MODULE = "RNSamsungHealth";

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";

    public static final String DAY_TIME = "day_time";

    private HealthDataStore mStore;

    public SamsungHealthModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return REACT_MODULE;
    }

    @Override
    public void initialize() {
        super.initialize();

        getReactApplicationContext().addLifecycleEventListener(this);
        initSamsungHealth();
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }

    private void sendEvent(String eventName,
                           @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    public void initSamsungHealth() {
        Log.d(REACT_MODULE, "initialize Samsung Health...");
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(getReactApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HealthDataStore getStore()
    {
        return mStore;
    }

    public ReactContext getContext()
    {
        return getReactApplicationContext();
    }

    @ReactMethod
    public void connect(Callback error, Callback success)
    {
        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(getReactApplicationContext(), new ConnectionListener(this, error, success));
        // Request the connection to the health data store
        mStore.connectService();
    }

    @ReactMethod
    public void disconnect()
    {
        if (mStore != null) {
            Log.d(REACT_MODULE, "disconnectService");
            mStore.disconnectService();
            mStore = null;
        }
    }

    /*
    private final HealthDataObserver mObserver = new HealthDataObserver(null) {
        // Update the step count when a change event is received
        @Override
        public void onChange(String dataTypeName) {
            Log.d(REACT_MODULE, "Observer receives a data changed event");
            readStepCount();
        }
    };

    private void start() {
        // Register an observer to listen changes of step count and get today step count
        // HealthDataObserver.addObserver(mStore, HealthConstants.StepCount.HEALTH_DATA_TYPE, mObserver);
        readStepCount();
    }
     */

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
    }

    // Read the today's step count on demand
    @ReactMethod
    public void readStepCount(double startDate, double endDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));
        Log.d(REACT_MODULE, "endDate:" + Long.toString((long)endDate));


        Filter filter = Filter.and(
            Filter.greaterThanEquals(HealthConstants.StepCount.START_TIME, (long)startDate),
            Filter.lessThanEquals(HealthConstants.StepCount.START_TIME, (long)endDate)
        );

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE) // "com.samsung.shealth.step_daily_trend"
                .setProperties(new String[]{
                        HealthConstants.StepCount.COUNT,       // "count"
                        HealthConstants.StepCount.START_TIME,  // "day_time"
                        HealthConstants.StepCount.END_TIME,  // "end_time"
                        HealthConstants.StepCount.DEVICE_UUID  // Common: "deviceuuid"
                })
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(new StepCountResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting step count fails.");
            error.invoke("Getting step count fails.");
        }
    }

    // Read the today's blood glucose on demand
    @ReactMethod
    public void readBloodGlucose(double startDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()

                .setDataType(HealthConstants.BloodGlucose.HEALTH_DATA_TYPE) //  "com.samsung.health.heart_rate"
                .setProperties(new String[]{
                        HealthConstants.BloodGlucose.GLUCOSE,       // "count"
                        HealthConstants.BloodGlucose.START_TIME,  // SessionMeasurement: "start_time"
                        HealthConstants.BloodGlucose.DEVICE_UUID  // Common: "deviceuuid"
                })
                .build();

        try {
            resolver.read(request).setResultListener(new BloodGlucoseResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting blood glucose fails.");
            error.invoke("Getting blood glucose fails.");
        }
    }

    // Read the today's blood pressure on demand
    @ReactMethod
    public void readBloodPressure(double startDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()

                .setDataType(HealthConstants.BloodPressure.HEALTH_DATA_TYPE) //  "com.samsung.health.blood_pressure"
                .setProperties(new String[]{
                        HealthConstants.BloodPressure.SYSTOLIC,       // Systolic value
                        HealthConstants.BloodPressure.DIASTOLIC,  // Diastolic value
                        HealthConstants.BloodPressure.PULSE,      // pulse
                        HealthConstants.BloodPressure.START_TIME, // start_time
                        HealthConstants.BloodPressure.DEVICE_UUID  // Common: "deviceuuid"
                })
                .build();

        try {
            resolver.read(request).setResultListener(new BloodPressureResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting blood pressure fails.");
            error.invoke("Getting blood pressure fails.");
        }
    }

    // -------------------------------------

    // write heart rate new value on demand
    @ReactMethod
    public void writeHeartRate(float heartRate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "heart rate:" + Long.toString((int)heartRate));

        HealthData data = new HealthData();
        data.putInt(HealthConstants.HeartRate.HEART_BEAT_COUNT, (int) heartRate * 60);
        data.putFloat(HealthConstants.HeartRate.HEART_RATE, heartRate);
        data.putLong(HealthConstants.HeartRate.START_TIME, new Date().getTime());
        data.putLong(HealthConstants.HeartRate.END_TIME, new Date().getTime());
        data.putLong(HealthConstants.HeartRate.TIME_OFFSET, 3600000);

        data.setSourceDevice(new HealthDeviceManager(mStore).getLocalDevice().getUuid());

        HealthDataResolver.InsertRequest request = new InsertRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .build();
        request.addHealthData(data);

        try {
            resolver.insert(request);
            success.invoke("Heart rate data inserted successfully");
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Write heart rate fails.");
            error.invoke("Write heart rate fails.");
        }
    }

    // write oxygen saturation new value on demand
    @ReactMethod
    public void writeOxygenSaturation(float oxygenSaturation, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "oxygen saturation:" + Float.toString(oxygenSaturation));

        HealthData data = new HealthData();
        data.putFloat(HealthConstants.OxygenSaturation.SPO2, oxygenSaturation);
        data.putLong(HealthConstants.OxygenSaturation.START_TIME, new Date().getTime());
        data.putLong(HealthConstants.OxygenSaturation.END_TIME, new Date().getTime());
        data.putLong(HealthConstants.OxygenSaturation.TIME_OFFSET, 3600000);

        data.setSourceDevice(new HealthDeviceManager(mStore).getLocalDevice().getUuid());

        HealthDataResolver.InsertRequest request = new InsertRequest.Builder()
                .setDataType(HealthConstants.OxygenSaturation.HEALTH_DATA_TYPE)
                .build();
        request.addHealthData(data);

        try {
            resolver.insert(request);
            success.invoke("Oxygen saturation data inserted successfully");
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Write oxygen saturation fails.");
            error.invoke("Write oxygen saturation fails.");
        }
    }

    // Read the today's heart rate on demand
    @ReactMethod
    public void readHeartRate(double startDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()

                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE) //  "com.samsung.health.heart_rate"
                .setProperties(new String[]{
                        HealthConstants.HeartRate.START_TIME,
                        HealthConstants.HeartRate.HEART_RATE,
                        HealthConstants.HeartRate.HEART_BEAT_COUNT,
                        HealthConstants.HeartRate.DEVICE_UUID  // Common: "deviceuuid"
                })
                .build();

        try {
            resolver.read(request).setResultListener(new HeartRateResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting heart rate fails.");
            error.invoke("Getting heart rate fails.");
        }
    }

    // Read the today's sleep on demand
    @ReactMethod
    public void readSleep(double startDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()

                .setDataType(HealthConstants.Sleep.HEALTH_DATA_TYPE) //  "com.samsung.health.sleep"
                .setProperties(new String[]{
                        HealthConstants.Sleep.TIME_OFFSET,  // time_offset
                        HealthConstants.Sleep.START_TIME,       // Start time
                        HealthConstants.Sleep.END_TIME,  // End time
                        HealthConstants.Sleep.DEVICE_UUID  // Common: "deviceuuid"
                })
                .build();

        try {
            resolver.read(request).setResultListener(new SleepResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting sleep fails.");
            error.invoke("Getting sleep fails.");
        }
    }

    // Read the today's weight on demand
    @ReactMethod
    public void readWeight(double startDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()

                .setDataType(HealthConstants.Weight.HEALTH_DATA_TYPE) //  "com.samsung.health.weight"
                .setProperties(new String[]{
                        HealthConstants.Weight.WEIGHT,       // Weight value
                        HealthConstants.Weight.FAT_FREE_MASS,  // Fat free mass value
                        HealthConstants.Weight.START_TIME, // start_time
                        HealthConstants.Weight.DEVICE_UUID  // Common: "deviceuuid"
                })
                .build();

        try {
            resolver.read(request).setResultListener(new WeightResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting weight fails.");
            error.invoke("Getting weight fails.");
        }
    }

    // Read the today's oxygen saturation on demand
    @ReactMethod
    public void readOxygenSaturation(double startDate, Callback error, Callback success) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        Log.d(REACT_MODULE, "startDate:" + Long.toString((long)startDate));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()

                .setDataType(HealthConstants.OxygenSaturation.HEALTH_DATA_TYPE) //  "com.samsung.health.oxygen_saturation"
                .setProperties(new String[]{
                        HealthConstants.OxygenSaturation.SPO2,       // SPO2 value
                        HealthConstants.OxygenSaturation.HEART_RATE,  // Heart rate value
                        HealthConstants.OxygenSaturation.START_TIME, // start_time
                        HealthConstants.OxygenSaturation.DEVICE_UUID  // Common: "deviceuuid"
                })
                .build();

        try {
            resolver.read(request).setResultListener(new OxygenSaturationResultListener(this, error, success));
        } catch (Exception e) {
            Log.e(REACT_MODULE, e.getClass().getName() + " - " + e.getMessage());
            Log.e(REACT_MODULE, "Getting oxygen saturation fails.");
            error.invoke("Getting oxygen saturation fails.");
        }
    }

    // -------------------------------------
}

/* vim :set ts=4 sw=4 sts=4 et : */
