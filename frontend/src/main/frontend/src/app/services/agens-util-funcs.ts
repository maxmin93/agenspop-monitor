export const DATE_UTILS = {

  toYYYYMMDD: (date: Date): string => {
    return date.toISOString().split('T')[0];
  },

  afterDays: (from: Date, days: number): Date => {
    let temp = new Date(from);
    temp.setDate(temp.getDate()+days);
    return temp;
  },

  diffDays: (from:Date, to:Date):number => {
    let diff = Math.abs(to.getTime() - from.getTime());
    return Math.ceil(diff / (1000 * 3600 * 24));
  }

};
