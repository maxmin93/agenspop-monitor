import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { from, throwError, of } from 'rxjs';
import { map, share, tap, catchError, retry, concatAll, timeout } from 'rxjs/operators';
import * as _ from 'lodash';
import { IQuery } from './agens-event-types';
import { IElement } from './agens-graph-types';

const TIMEOUT_LIMIT:number = 9999;

@Injectable({
  providedIn: 'root'
})
export class AmApiService {

  // apiUrl = `${window.location.protocol}//${window.location.host}`;
  apiUrl = `http://localhost:8082`;

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
  // http://localhost:8082/agens/datasources
  public findDatasources() {
    let uri = this.apiUrl+'/agens/datasources';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  // queries
  // http://localhost:8080/query
  public findQueries() {
    let uri = this.apiUrl+'/query';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  // queries
  // http://localhost:8080/query
  public findQuery(qid:number) {
    let uri = this.apiUrl+'/query/'+qid;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  // queries
  // http://localhost:8080/query
  public findQueryWithDateRange(qid:number) {
    let uri = this.apiUrl+'/query/'+qid+'/date-range';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        retry(3), // retry a failed request up to 3 times
        catchError(this.handleError) // then handle the error
      );
  }

  public addQuery(query:IQuery) {
    let uri = this.apiUrl+'/query';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.post( uri, query, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public updateQuery(query:IQuery) {
    let uri = this.apiUrl+'/query/'+query.id;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.put( uri, query, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public changeStateQuery(qid:number, state:boolean) {
    let uri = this.apiUrl+'/query/'+qid+'/change-state?state='+state;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public deleteQuery(qid:number) {
    let uri = this.apiUrl+'/query/'+qid;
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

  public execGremlin(datasource:string, script:string){
    let uri = this.apiUrl+'/agens/gremlin';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    let params = { datasource: datasource, q: script };
    return this._http.post<IElement[]>( uri, params, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public execGremlinWithRange(datasource:string, script:string, from:string, to?:string){
    let uri = this.apiUrl+'/agens/gremlin/range';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    let params = { datasource: datasource, q: script, from: from, to: to };
    return this._http.post<IElement[]>( uri, params, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public findConnectedEdges(datasource:string, ids:string[]){
    let uri = this.apiUrl+'/agens/connected_edges';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    let params = { datasource: datasource, q: ids.join(',') };
    return this._http.post<IElement[]>( uri, params, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public findConnectedVertices(datasource:string, ids:string[]){
    let uri = this.apiUrl+'/agens/connected_vertices';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    let params = { datasource: datasource, q: ids.join(',') };
    return this._http.post<IElement[]>( uri, params, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public findNeighborVertices(datasource:string, ids:string[]){
    let uri = this.apiUrl+'/agens/neighbors';
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    let params = { datasource: datasource, q: ids.join(',') };
    return this._http.post<IElement[]>( uri, params, { headers : headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  public findEventsWithDateRange(qid:number, from_date:string, to_date:string){
    let uri = this.apiUrl+'/rows/search?qid='+qid+'&from='+from_date+'&to='+to_date;
    let headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this._http.get<any>( uri, { headers : headers })
    .pipe(
      retry(3), // retry a failed request up to 3 times
      catchError(this.handleError) // then handle the error
    );
  }
}
