import {RoomType} from './room.model';

export interface RoomRequest {
  dormitoryId: number;
  number: number;
  type: RoomType;
  capacity: number;
  floor: number;
  cost: number;
}
