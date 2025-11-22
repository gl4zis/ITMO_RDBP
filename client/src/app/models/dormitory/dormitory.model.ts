import {DormitoryRequest} from './dormitory.request';

export interface Dormitory extends DormitoryRequest {
  id: number;
  residentNumber: number;
}
