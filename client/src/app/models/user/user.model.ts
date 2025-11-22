import {Role} from '../auth/role.model';

export interface User {
  login: string;
  name: string;
  surname: string;
  role: Role;
}
