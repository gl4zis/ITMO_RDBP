import {UniversityRequest} from './university.request';

export interface University extends UniversityRequest {
  id: number;
  dormitoryIds: number[];
}
