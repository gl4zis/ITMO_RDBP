import {PaymentHistory} from './payment-history.model';

export interface PaymentInfo {
  debt: number;
  roomCost: number;
  lastPaymentTime: Date;
  history: PaymentHistory[];
}
