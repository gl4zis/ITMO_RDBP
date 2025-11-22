import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environment/environment';
import {Observable} from 'rxjs';
import {Resident} from '../models/user/resident.model';
import {User} from '../models/user/user.model';
import {Eviction} from '../models/eviction.model';

@Injectable({
  providedIn: 'root'
})
export class UserRepository {
  private api = `${environment.api}/user`;

  constructor(private http: HttpClient) { }

  getStaff(): Observable<User[]> {
    return this.http.get<User[]>(`${this.api}/staff`);
  }

  getResidents(): Observable<Resident[]> {
    return this.http.get<Resident[]>(`${this.api}/residents`);
  }

  fire(login: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/fire?login=${login}`, {});
  }

  getEvictions(): Observable<Eviction[]> {
    return this.http.get<Eviction[]>(`${this.api}/residents/to-eviction`);
  }

  evict(login: string): Observable<Eviction[]> {
    return this.http.post<Eviction[]>(`${this.api}/residents/evict?login=${login}`, {});
  }
}
