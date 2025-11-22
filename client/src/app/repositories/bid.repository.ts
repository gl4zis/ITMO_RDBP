import {Injectable} from '@angular/core';
import {environment} from '../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Bid, BidType} from '../models/bid/bid.model';
import {BidRequest, DepartureRequest, OccupationRequest, RoomChangeRequest} from '../models/bid/bid.request';

@Injectable({
  providedIn: 'root'
})
export class BidRepository {
  private api = `${environment.api}/bid`;

  constructor(private http: HttpClient) {}

  getSelf(): Observable<Bid[]> {
    return this.http.get<Bid[]>(`${this.api}/my`);
  }

  get(id: number): Observable<Bid> {
    return this.http.get<Bid>(`${this.api}/${id}`);
  }

  getInProcess(): Observable<Bid[]> {
    return this.http.get<Bid[]>(`${this.api}/in-process`);
  }

  getPending(): Observable<Bid[]> {
    return this.http.get<Bid[]>(`${this.api}/pending`);
  }

  getArchived(): Observable<Bid[]> {
    return this.http.get<Bid[]>(`${this.api}/archived`);
  }

  editBid(id: number, req: BidRequest, type: BidType): Observable<void> {
    switch (type) {
      case BidType.DEPARTURE:
        return this.editDeparture(id, req as DepartureRequest);
      case BidType.OCCUPATION:
        return this.editOccupation(id, req as OccupationRequest);
      case BidType.EVICTION:
        return this.editEviction(id, req);
      case BidType.ROOM_CHANGE:
        return this.editRoomChange(id, req as RoomChangeRequest);
    }
  }

  createBid(req: BidRequest, type: BidType): Observable<void> {
    switch (type) {
      case BidType.DEPARTURE:
        return this.createDeparture(req as DepartureRequest);
      case BidType.OCCUPATION:
        return this.createOccupation(req as OccupationRequest);
      case BidType.EVICTION:
        return this.createEviction(req);
      case BidType.ROOM_CHANGE:
        return this.createRoomChange(req as RoomChangeRequest);
    }
  }

  private editDeparture(id: number, req: DepartureRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/departure/${id}`, req);
  }

  private editOccupation(id: number, req: OccupationRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/occupation/${id}`, req);
  }

  private editEviction(id: number, req: BidRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/eviction/${id}`, req);
  }

  private editRoomChange(id: number, req: RoomChangeRequest): Observable<void> {
    return this.http.put<void>(`${this.api}/room-change/${id}`, req);
  }

  private createOccupation(req: OccupationRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/occupation`, req);
  }

  private createEviction(req: BidRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/eviction`, req);
  }

  private createDeparture(req: DepartureRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/departure`, req);
  }

  private createRoomChange(req: RoomChangeRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/room-change`, req);
  }

  deny(id: number, comment: string): Observable<void> {
    return this.http.post<void>(`${this.api}/${id}/deny`, { data: comment });
  }

  accept(id: number): Observable<void> {
    return this.http.post<void>(`${this.api}/${id}/accept`, {});
  }

  pend(id: number, comment: string): Observable<void> {
    return this.http.post<void>(`${this.api}/${id}/pend`, { data: comment });
  }

  getSelfOpenedBidTypes(): Observable<BidType[]> {
    return this.http.get<BidType[]>(`${this.api}/my/opened-types`);
  }
}
