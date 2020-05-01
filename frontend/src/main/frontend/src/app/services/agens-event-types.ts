export interface IQuery {
  id: number;
  datasource: string;
  name: string;
  query: string;
  active_yn: boolean;
  cr_date: Date;
  up_date: Date;
};

export interface IAggregation {
  id: number;
  edate: Date;
  qid: number;
  type: string;
  labels: string;
  row_cnt: number;
  ids_cnt: number;
};
