import {Injectable} from '@angular/core';
import {environment} from '../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PaymentInfo} from '../models/payment/payment-info.model';
import {PaymentRequest} from '../models/payment/payment.request';

@Injectable({
  providedIn: 'root'
})
export class PaymentRepository {
  private api = `${environment.api}/payment`;

  constructor(private http: HttpClient) {}

  getSelfPaymentInfo(): Observable<PaymentInfo> {
    return this.http.get<PaymentInfo>(`${this.api}/info/self`);
  }

  getPaymentInfo(login: string): Observable<PaymentInfo> {
    return this.http.get<PaymentInfo>(`${this.api}/info?login=${login}`);
  }

  pay(req: PaymentRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/pay`, req);
  }
}
