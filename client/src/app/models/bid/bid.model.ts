import {User} from '../user/user.model';
import {RoomType} from '../room/room.model';
import {Role} from '../auth/role.model';

export interface Bid {
  number: number;
  sender?: User;
  manager?: User;
  text: string;
  comment?: string;
  type: BidType;
  attachments: Attachment[];
  status: BidStatus;
}

export interface Attachment {
  filename: string;
  downloadKey: string;
}

export interface OccupationData {
  universityId: number;
  dormitoryId: number;
}

export interface DepartureData {
  dayFrom: Date;
  dayTo: Date;
}

export interface RoomChangeData {
  roomToId?: number;
  roomPreferType?: RoomType;
}

export interface OccupationBid extends Bid, OccupationData {}

export interface DepartureBid extends Bid, DepartureData {}

export interface RoomChangeBid extends Bid, RoomChangeData {}

export enum BidStatus {
  IN_PROCESS = 'IN_PROCESS',
  PENDING_REVISION = 'PENDING_REVISION',
  ACCEPTED = 'ACCEPTED',
  DENIED = 'DENIED'
}

export function isEditableBidStatus(status: BidStatus): boolean {
  return status === BidStatus.IN_PROCESS || status === BidStatus.PENDING_REVISION;
}

export enum BidType {
  OCCUPATION = 'OCCUPATION',
  DEPARTURE = 'DEPARTURE',
  ROOM_CHANGE = 'ROOM_CHANGE',
  EVICTION = 'EVICTION'
}

export const BID_STATUS_COLOR_MAP = {
  [BidStatus.IN_PROCESS]: '#aaa',
  [BidStatus.PENDING_REVISION]: '#ca0',
  [BidStatus.ACCEPTED]: '#0b0',
  [BidStatus.DENIED]: '#b00',
}

export const BID_TYPE_MAP = {
  [BidType.OCCUPATION]: 'заселение',
  [BidType.EVICTION]: 'выселение',
  [BidType.DEPARTURE]: 'отъезд',
  [BidType.ROOM_CHANGE]: 'смену комнаты'
};

export const BID_TYPE_RESIDENT_OPTIONS = [
  { id: BidType.EVICTION, name: 'Выселение' },
  { id: BidType.DEPARTURE, name: 'Временный отъезд' },
  { id: BidType.ROOM_CHANGE, name: 'Смена комнаты' }
];

export const BID_TYPE_NON_RESIDENT_OPTIONS = [
  { id: BidType.OCCUPATION, name: 'Заселение' }
];
