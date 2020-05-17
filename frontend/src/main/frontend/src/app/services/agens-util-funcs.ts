export const DATE_UTILS = {

  toYYYYMMDD: (date: Date): string => {
    return date.toISOString().split('T')[0];
  },
  toHHMMDD: (date: Date): string => {
    return date.toISOString().split('T')[1].split('.')[0];
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
