import {
  NativeModules,
  DeviceEventEmitter,
} from 'react-native';

const samsungHealth = NativeModules.RNSamsungHealth;

class RNSamsungHealth {
  constructor() {
  }

  authorize = (callback) => {
    samsungHealth.connect(
      (msg) => { callback(msg, false); },
      (res) => { callback(false, res); },
    );
  }

  stop = () => {
    samsungHealth.disconnect();
  }

  getDailyStepCountSamples(options, callback) {
    console.log('getDailyStepCounts');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);
    const endDate = options.endDate !== undefined
      ? Date.parse(options.endDate) : (new Date()).valueOf();
    const mergeData = options.mergeData !== undefined ? options.mergeData : true;

    samsungHealth.readStepCount(startDate, endDate,
      (msg) => { callback(msg, false); },
      (res) => {
        if (res.length > 0) {
          let resData = res.map((dev) => {
            const obj = {};
            obj.source = dev.source.name;
            obj.values = this.buildDailySteps(dev.steps);
            obj.sourceDetail = dev.source;
            return obj;
          }, this);
          if (mergeData) {
            resData = this.mergeResult(resData);
            console.log(resData);
          }
          callback(false, resData);
        } else {
          callback('There is no any steps data for this period', false);
        }
      });
  }

  getBloodGlucoseSamples(options, callback) {
    console.log('getBloodGlucose');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);

    samsungHealth.readBloodGlucose(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        if (res.length > 0) {
          const resData = res.map((dev) => {
            const obj = {};
            console.log(dev);
            obj.source = dev.source.name;
            obj.sourceDetail = dev.source;
            const values = dev.bloodGlucose.map(val => ({
              value: val.glucose,
              startDate: new Date(val.start_time),
            }));
            obj.values = values;
            console.log(obj);
            return obj;
          }, this);
          console.log('risultato della lettura della glicemia');
          callback(false, resData);
        } else {
          callback('There is no any blood glucose data for this period', false);
        }
      });
  }

  getOxygenSaturationSamples(options, callback) {
    console.log('getOxygenSaturation');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);

    samsungHealth.readOxygenSaturation(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        if (res.length > 0) {
          const resData = res.map((dev) => {
            const obj = {};
            console.log(dev);
            obj.source = dev.source.name;
            obj.sourceDetail = dev.source;
            const values = dev.oxygenSaturation.map(val => ({
              value: val.spo2,
              startDate: new Date(val.start_time),
              heartRate: val.heart_rate,
            }));
            obj.values = values;
            console.log(obj);
            return obj;
          }, this);
          console.log("risultato della lettura della saturazione dell'ossigeno");
          callback(false, resData);
        } else {
          callback('There is no any oxygen saturation data for this period', false);
        }
      });
  }

  putOxygenSaturation = (value, callback) => {
    console.log('putOxygenSaturation');

    samsungHealth.writeOxygenSaturation(value,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        callback();
      });
  }

  getHeartRateSamples(options, callback) {
    console.log('getHeartRate');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);

    samsungHealth.readHeartRate(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        if (res.length > 0) {
          const resData = res.map((dev) => {
            const obj = {};
            console.log(dev);
            obj.source = dev.source.name;
            obj.sourceDetail = dev.source;
            const values = dev.heartRate.map(val => ({
              value: val.heart_rate,
              startDate: new Date(val.start_time),
            }));
            obj.values = values;
            console.log(obj);
            return obj;
          }, this);
          console.log('risultato della lettura della glicemia');
          callback(false, resData);
        } else {
          callback('There is no any blood glucose data for this period', false);
        }
      });
  }

  putHeartRate = (value, callback) => {
    console.log('putHeartRate');

    samsungHealth.writeHeartRate(value,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        callback();
      });
  }

  getBloodPressureSamples = (options, callback) => {
    console.log('getBloodPressure');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);

    samsungHealth.readBloodPressure(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        if (res.length > 0) {
          const resData = res.map((dev) => {
            const obj = {};
            console.log(dev);
            obj.source = dev.source.name;
            obj.sourceDetail = dev.source;
            const values = dev.bloodPressure.map(val => ({
              bloodPressureSystolicValue: val.systolic,
              bloodPressureDiastolicValue: val.diastolic,
              pulse: val.pulse,
              startDate: new Date(val.start_time),
            }));
            obj.values = values;
            console.log(obj);
            return obj;
          }, this);
          console.log('risultato della lettura della pressione');
          callback(false, resData);
        } else {
          callback('There is no any blood pressure data for this period', false);
        }
      });
  }

  getWeightSamples(options, callback) {
    console.log('getWeight');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);

    samsungHealth.readWeight(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        if (res.length > 0) {
          const resData = res.map((dev) => {
            const obj = {};
            console.log(dev);
            obj.source = dev.source.name;
            obj.sourceDetail = dev.source;
            const values = dev.weight.map(val => ({
              value: val.weight * 1000,
              leanBodyMass: val.fat_free_mass,
              startDate: new Date(val.start_time),
            }));
            obj.values = values;
            console.log(obj);
            return obj;
          }, this);
          console.log('risultato della lettura del peso');
          callback(false, resData);
        } else {
          callback('There is no any weight data for this period', false);
        }
      });
  }

  getSleepSamples(options, callback) {
    console.log('getSleep');

    const startDate = options.startDate !== undefined
      ? Date.parse(options.startDate) : (new Date()).setHours(0, 0, 0, 0);

    samsungHealth.readSleep(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
        console.log(res);
        if (res.length > 0) {
          const resData = res.map((dev) => {
            const obj = {};
            console.log(dev);
            obj.source = dev.source.name;
            obj.sourceDetail = dev.source;
            const values = dev.sleep.map(val => ({
              startDate: new Date(val.start_time).toISOString(),
              endDate: new Date(val.end_time).toISOString(),
              value: 'ASLEEP',
            }));
            obj.values = values;
            console.log(obj);
            return obj;
          }, this);
          console.log('risultato della lettura del sonno');
          callback(false, resData);
        } else {
          callback('There is no any sleep data for this period', false);
        }
      });
  }

  usubscribeListeners = () => {
    DeviceEventEmitter.removeAllListeners();
  }

  buildDailySteps = (steps) => {
    console.log(steps);

    const results = {};

    for (const step of steps) {
      let date = step.start_time !== undefined
        ? new Date(step.start_time) : new Date(step.day_time);
      date = date.toUTCString();
      date = date.split(' ').slice(0, 4).join(' ');

      if (!(date in results)) {
        results[date] = 0;
      }

      results[date] += step.count;
    }

    const results2 = [];
    for (const index in results) {
      results2.push({ startDate: new Date(index), value: results[index] });
    }

    console.log(results2);
    return results2;
  }

  mergeResult = (res) => {
    const results = {};
    for (const dev of res) {
      if (!(dev.sourceDetail.group in results)) {
        results[dev.sourceDetail.group] = {
          source: dev.source,
          sourceDetail: { group: dev.sourceDetail.group },
          stepsDate: {},
        };
      }

      const group = results[dev.sourceDetail.group];

      for (const step of dev.steps) {
        if (!(step.date in group.stepsDate)) {
          group.stepsDate[step.date] = 0;
        }

        group.stepsDate[step.date] += step.value;
      }
    }

    const results2 = [];
    for (const index in results) {
      const group = results[index];
      const steps = [];
      for (const date in group.stepsDate) {
        steps.push({
          date,
          value: group.stepsDate[date],
        });
      }
      group.steps = steps.sort((a, b) => (a.date < b.date ? -1 : 1));
      delete group.stepsDate;

      results2.push(group);
    }

    return results2;
  }
}

export default new RNSamsungHealth();

/* vim :set ts=4 sw=4 sts=4 et : */
