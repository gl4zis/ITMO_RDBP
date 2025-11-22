import {User} from './user.model';
import {University} from '../university/university.model';
import {Dormitory} from '../dormitory/dormitory.model';

export interface Resident extends User {
  university: University;
  dormitory: Dormitory;
  roomNumber: number;
  debt: number;
  lastCameOut?: Date;
}
