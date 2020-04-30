import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { from, throwError, of } from 'rxjs';
import { map, share, tap, catchError, concatAll, timeout } from 'rxjs/operators';
import * as _ from 'lodash';

const TIMEOUT_LIMIT:number = 9999;

@Injectable({
  providedIn: 'root'
})
export class AmApiService {

  // apiUrl = `${window.location.protocol}//${window.location.host}`;
  apiUrl = `http://localhost:8080`;

  constructor(private _http: HttpClient) { }

  // queries
  // http://localhost:8080/queries
  public findQueries() {
    let uri = this.apiUrl+'/queries';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers });
  }

  // aggregations
  // http://localhost:8080/aggs
  public findAggregations() {
    let uri = this.apiUrl+'/aggs';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers });
  }

  // aggregations
  // http://localhost:8080/aggs
  public findAggregationsByQid(qid:number) {
    let uri = this.apiUrl+'/aggs/qid/'+qid;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers });
  }

}
