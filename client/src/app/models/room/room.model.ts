import {RoomRequest} from './room.request';
import {User} from '../user/user.model';

export interface Room extends RoomRequest {
  id: number;
  residents: User[];
}

export enum RoomType {
  BLOCK = 'BLOCK',
  AISLE = 'AISLE'
}

export const ROOM_TYPE_OPTIONS = [
  {id: 0, name: '-'},
  {id: RoomType.AISLE, name: 'Коридорка'},
  {id: RoomType.BLOCK, name: 'Блок'}
];

export function localizeRoomType(type: RoomType): string {
  return ROOM_TYPE_OPTIONS.find(o => o.id === type)!.name;
}
