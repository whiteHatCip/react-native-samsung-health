import { 
  NativeModules,
  DeviceEventEmitter
} from 'react-native';

const samsungHealth = NativeModules.RNSamsungHealth;

class RNSamsungHealth {
  constructor() {
  }

  authorize(callback) {
    samsungHealth.connect(
      (msg) => { callback(msg, false); },
      (res) => { callback(false, res); }
    );
  }

  stop() {
    samsungHealth.disconnect();
  }

  getDailyStepCountSamples(options, callback) {
    console.log("getDailyStepCounts");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let endDate = options.endDate != undefined ? Date.parse(options.endDate) : (new Date()).valueOf();
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    // console.log("startDate:" + startDate);
    // console.log("endDate:" + endDate);
    // console.log("startDate2:" + (new Date(startDate)).toLocaleString());
    // console.log("endDate2:" + (new Date(endDate)).toLocaleString());

    samsungHealth.readStepCount(startDate, endDate,
      (msg) => { callback(msg, false); },
      (res) => {
          if (res.length>0) {
              var resData = res.map(function(dev) {
                  var obj = {};
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
              callback("There is no any steps data for this period", false);
          }
      }
    );
  }

  getBloodGlucoseSamples(options, callback) {
    console.log("getBloodGlucose");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    samsungHealth.readBloodGlucose(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
          console.log(res);
          if (res.length>0) {
              var resData = res.map((dev) =>  {
                  var obj = {};
                  var values = [];
                  console.log(dev);
                  obj.source = dev.source.name;
                  obj.sourceDetail = dev.source;
                  for(var val of dev.bloodGlucose) {
                    values.push({ value: val.glucose, startDate: new Date(val.start_time) });
                  }
                  obj.values = values;
                  console.log(obj);
                  return obj;
                }, this);
                console.log("risultato della lettura della glicemia");
              callback(false, resData);
          } else {
              callback("There is no any blood glucose data for this period", false);
          }
      }
    );
  }

  getHeartRateSamples(options, callback) {
    console.log("getHeartRate");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    samsungHealth.readHeartRate(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
          console.log(res);
          if (res.length>0) {
              var resData = res.map((dev) =>  {
                  var obj = {};
                  var values = [];
                  console.log(dev);
                  obj.source = dev.source.name;
                  obj.sourceDetail = dev.source;
                  for(var val of dev.heartRate) {
                    values.push({ value: val.heart_rate, startDate: new Date(val.start_time) });
                  }
                  obj.values = values;
                  console.log(obj);
                  return obj;
                }, this);
                console.log("risultato della lettura della glicemia");
              callback(false, resData);
          } else {
              callback("There is no any blood glucose data for this period", false);
          }
      }
    );
  }

  putHeartRate(options, callback) {
    console.log("putHeartRate");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    samsungHealth.writeHeartRate(54,
      (msg) => { callback(msg, false); },
      (res) => {
          console.log(res);
          if (res.length>0) {
              var resData = res.map((dev) =>  {
                  var obj = {};
                  var values = [];
                  console.log(dev);
                  obj.source = dev.source.name;
                  obj.sourceDetail = dev.source;
                  for(var val of dev.bloodGlucose) {
                    values.push({ value: val.glucose, startDate: new Date(val.start_time) });
                  }
                  obj.values = values;
                  console.log(obj);
                  return obj;
                }, this);
                console.log("risultato della lettura della glicemia");
              callback(false, resData);
          } else {
              callback("There is no any blood glucose data for this period", false);
          }
      }
    );
  }

  getBloodPressureSamples(options, callback) {
    console.log("getBloodPressure");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    samsungHealth.readBloodPressure(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
          console.log(res);
          if (res.length>0) {
              var resData = res.map((dev) =>  {
                  var obj = {};
                  var values = [];
                  console.log(dev);
                  obj.source = dev.source.name;
                  obj.sourceDetail = dev.source;
                  for(var val of dev.bloodPressure) {
                    values.push({
                        bloodPressureSystolicValue: val.systolic,
                        bloodPressureDiastolicValue: val.diastolic,
                        pulse: val.pulse,
                        startDate: new Date(val.start_time)
                    });
                  }
                  obj.values = values;
                  console.log(obj);
                  return obj;
                }, this);
                console.log("risultato della lettura della pressione");
              callback(false, resData);
          } else {
              callback("There is no any blood pressure data for this period", false);
          }
      }
    );
  }

  getWeightSamples(options, callback) {
    console.log("getWeight");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    samsungHealth.readWeight(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
          console.log(res);
          if (res.length>0) {
              var resData = res.map((dev) =>  {
                  var obj = {};
                  var values = [];
                  console.log(dev);
                  obj.source = dev.source.name;
                  obj.sourceDetail = dev.source;
                  for(var val of dev.weight) {
                    values.push({
                        value: val.weight * 1000,
                        leanBodyMass: val.fat_free_mass,
                        startDate: new Date(val.start_time)
                    });
                  }
                  obj.values = values;
                  console.log(obj);
                  return obj;
                }, this);
                console.log("risultato della lettura del peso");
              callback(false, resData);
          } else {
              callback("There is no any weight data for this period", false);
          }
      }
    );
  }

  getSleepSamples(options, callback) {
    console.log("getSleep");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    samsungHealth.readSleep(startDate,
      (msg) => { callback(msg, false); },
      (res) => {
          console.log(res);
          if (res.length>0) {
              var resData = res.map((dev) =>  {
                  var obj = {};
                  var values = [];
                  console.log(dev);
                  obj.source = dev.source.name;
                  obj.sourceDetail = dev.source;
                  for(var val of dev.sleep) {
                    values.push({
                        startDate: new Date(val.start_time).toISOString(),
                        endDate: new Date(val.end_time).toISOString(),
                        value: 'ASLEEP',
                    });
                  }
                  obj.values = values;
                  console.log(obj);
                  return obj;
                }, this);
                console.log("risultato della lettura del sonno");
              callback(false, resData);
          } else {
              callback("There is no any sleep data for this period", false);
          }
      }
    );
  }

  usubscribeListeners() {
    DeviceEventEmitter.removeAllListeners();
  }

  buildDailySteps(steps)
  {
          console.log(steps);

      results = {}
      for(var step of steps) {
          var date = step.start_time !== undefined ? new Date(step.start_time) : new Date(step.day_time);
          date = date.toUTCString();
          date = date.split(' ').slice(0, 4).join(' ');

          if (!(date in results)) {
              results[date] = 0;
          }

          results[date] += step.count;
      }

      results2 = [];
      for(var index in results) {
          results2.push({startDate: new Date(index), value: results[index]});
      }
      console.log(results2);
      return results2;
  }

  mergeResult(res)
  {
      results = {}
      for(var dev of res)
      {
          if (!(dev.sourceDetail.group in results)) {
              results[dev.sourceDetail.group] = {
                  source: dev.source,
                  sourceDetail: { group: dev.sourceDetail.group },
                  stepsDate: {}
              };
          }

          let group = results[dev.sourceDetail.group];

          for (var step of dev.steps) {
              if (!(step.date in group.stepsDate)) {
                  group.stepsDate[step.date] = 0;
              }

              group.stepsDate[step.date] += step.value;
          }
      }

      results2 = [];
      for(var index in results) {
          let group = results[index];
          var steps = [];
          for(var date in group.stepsDate) {
              steps.push({
                date: date,
                value: group.stepsDate[date]
              });
          }
          group.steps = steps.sort((a,b) => a.date < b.date ? -1 : 1);
          delete group.stepsDate;

          results2.push(group);
      }

      return results2;
  }

}

export default new RNSamsungHealth();

/* vim :set ts=4 sw=4 sts=4 et : */
