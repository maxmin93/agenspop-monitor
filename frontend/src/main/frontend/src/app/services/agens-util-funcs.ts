export const DATE_UTILS = {

  toYYYYMMDD: (date: Date): string => {
    let year = date.getFullYear();
    let month = date.getMonth()+1;    // from 0 ~ to 11
    let day = date.getDate();
    return `${year}-${month < 10 ? '0'+month : ''+month}-${day < 10 ? '0'+day: ''+day}`;
  },
  toHHMMDD: (date: Date): string => {
    // **NOTE: toISODateString 은 zoneOffset=0 기반이라 시간이 맞지 않는다
    let hour = date.getHours();
    let minute = date.getMinutes();
    let second = date.getSeconds();
    return `${hour < 10 ? '0'+hour : ''+hour}:${minute < 10 ? '0'+minute : ''+minute}:${second < 10 ? '0'+second: ''+second}`;
  },

  afterDays: (from: Date, days: number): Date => {
    let temp = new Date(from);
    temp.setDate(temp.getDate()+days);
    return temp;
  },
  afterMinutes: (from: Date, minutes: number): Date => {
    let temp = new Date(from);
    temp.setTime(temp.getTime()+ (minutes * 60 * 1000));
    return temp;
  },
  afterSeconds: (from: Date, seconds: number): Date => {
    let temp = new Date(from);
    temp.setTime(temp.getTime()+ (seconds * 1000));
    return temp;
  },
  // 30초 단위 자르기 : calculateNextCycle(Date.now(), 30 * 1000)
  calcNextCycle: (timeStampCurrent: number, interval:number):Date => {
    // let timeStampCurrent = Date.now();
    let timeStampStartOfHour = new Date().setMinutes(0, 0, 0);
    let timeDiff = timeStampCurrent - timeStampStartOfHour;
    let mod = Math.ceil(timeDiff / interval);
    return new Date(timeStampStartOfHour + (mod * interval));
  },

  diffDays: (from:Date, to:Date):number => {
    let diff = Math.abs(to.getTime() - from.getTime());
    return Math.ceil(diff / (1000 * 60 * 60 * 24));       // divide by 1 day
  },
  diffMinutes: (from:Date, to:Date):number => {
    let diff = Math.abs(to.getTime() - from.getTime());
    return Math.ceil(diff / (1000 * 60));                 // divide by 1 minute
  },
  diffSeconds: (from:Date, to:Date):number => {
    let diff = Math.abs(to.getTime() - from.getTime());
    return Math.ceil(diff / (1000));                      // divide by 1 second
  }

};
