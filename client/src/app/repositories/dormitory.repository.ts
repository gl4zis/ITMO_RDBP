import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environment/environment';
import {Observable} from 'rxjs';
import {Dormitory} from '../models/dormitory/dormitory.model';
import {DormitoryRequest} from '../models/dormitory/dormitory.request';

@Injectable({
  providedIn: 'root'
})
export class DormitoryRepository {
  private api = `${environment.api}/dormitory`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Dormitory[]> {
    return this.http.get<Dormitory[]>(`${this.api}`);
  }

  get(id: number): Observable<Dormitory> {
    return this.http.get<Dormitory>(`${this.api}/${id}`);
  }

  add(req: DormitoryRequest): Observable<void> {
    return this.http.post<void>(`${this.api}`, req);
  }

  update(id: number, req: DormitoryRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
