import {Injectable} from '@angular/core';
import {environment} from '../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {GuardHistory} from '../models/guard-history.model';

@Injectable({
  providedIn: 'root'
})
export class GuardRepository {
  private api = `${environment.api}/guard`;

  constructor(private http: HttpClient) {}

  entry(login: string): Observable<void> {
    return this.http.post<void>(`${this.api}/entry?login=${login}`, {});
  }

  exit(login: string): Observable<void> {
    return this.http.post<void>(`${this.api}/exit?login=${login}`, {});
  }

  getHistory(login: string): Observable<GuardHistory[]> {
    return this.http.get<GuardHistory[]>(`${this.api}/history?login=${login}`);
  }

  getSelfHistory(): Observable<GuardHistory[]> {
    return this.http.get<GuardHistory[]>(`${this.api}/history/self`);
  }
}
