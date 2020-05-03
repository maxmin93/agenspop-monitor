import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { from, throwError, of } from 'rxjs';
import { map, share, tap, catchError, retry, concatAll, timeout } from 'rxjs/operators';
import * as _ from 'lodash';
import { IQuery } from './agens-event-types';

const TIMEOUT_LIMIT:number = 9999;

@Injectable({
  providedIn: 'root'
})
export class AmApiService {

  // apiUrl = `${window.location.protocol}//${window.location.host}`;
  apiUrl = `http://localhost:8080`;

  constructor(private _http: HttpClient) { }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${error.error}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  };

  // queries
  // http://localhost:8080/queries
  public findQueries() {
    let uri = this.apiUrl+'/queries';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  // queries
  // http://localhost:8080/queries
  public findQuery(qid:number) {
    let uri = this.apiUrl+'/queries/'+qid;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  public addQuery(query:IQuery) {
    let uri = this.apiUrl+'/queries';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.post( uri, query, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public updateQuery(query:IQuery) {
    let uri = this.apiUrl+'/queries/'+query.id;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.put( uri, query, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public changeStateQuery(qid:number, state:boolean) {
    let uri = this.apiUrl+'/queries/'+qid+'/changeState?state='+state;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public deleteQuery(qid:number) {
    let uri = this.apiUrl+'/queries/'+qid;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.delete( uri, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  // aggregations
  // http://localhost:8080/aggs
  public findAggregations() {
    let uri = this.apiUrl+'/aggs';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  // aggregations
  // http://localhost:8080/aggs
  public findAggregationsByQid(qid:number) {
    let uri = this.apiUrl+'/aggs/qid/'+qid;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
    .pipe(
      retry(3), // retry a failed request up to 3 times
      catchError(this.handleError) // then handle the error
    );
  }

}
