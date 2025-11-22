import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environment/environment';
import {Observable} from 'rxjs';
import {Room} from '../models/room/room.model';
import {RoomRequest} from '../models/room/room.request';

@Injectable({
  providedIn: 'root'
})
export class RoomRepository {
  private api = `${environment.api}/room`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.api}`);
  }

  getAvailableForResident(): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.api}/for-resident`);
  }

  get(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.api}/${id}`);
  }

  add(req: RoomRequest): Observable<void> {
    return this.http.post<void>(`${this.api}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
