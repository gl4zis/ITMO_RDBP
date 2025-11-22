import {Injectable} from '@angular/core';
import {environment} from '../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {University} from '../models/university/university.model';
import {UniversityRequest} from '../models/university/university.request';

@Injectable({
  providedIn: 'root'
})
export class UniversityRepository {
  private api = `${environment.api}/university`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<University[]> {
    return this.http.get<University[]>(`${this.api}`);
  }

  get(id: number): Observable<University> {
    return this.http.get<University>(`${this.api}/${id}`);
  }

  add(req: UniversityRequest): Observable<void> {
    return this.http.post<void>(`${this.api}`, req);
  }

  update(id: number, req: UniversityRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
