export interface GuardHistory {
  type: GuardType;
  timestamp: Date;
}

export enum GuardType {
  IN = 'IN',
  OUT = 'OUT'
}
