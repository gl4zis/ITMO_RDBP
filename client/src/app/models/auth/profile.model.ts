import {Role} from './role.model';

export interface Profile {
  name: string;
  surname: string;
  role: Role;
  university?: string;
  dormitory?: string;
  roomNumber?: number;
}
