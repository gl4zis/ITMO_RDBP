import {Injectable} from '@angular/core';
import {environment} from '../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Notification} from '../models/notification/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationRepository {
  private api = `${environment.api}/notification`;

  constructor(private http: HttpClient) {}

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.api}/unread`);
  }

  markAsRead(notificationId: number): Observable<void> {
    return this.http.post<void>(`${this.api}/mark-as-read?id=${notificationId}`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.post<void>(`${this.api}/mark-all-as-read`, {});
  }
}
