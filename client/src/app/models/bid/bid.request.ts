import {DepartureData, OccupationData, RoomChangeData} from './bid.model';

export interface BidRequest {
  text: string;
  attachmentKeys: string[];
}

export interface OccupationRequest extends BidRequest, OccupationData {}

export interface DepartureRequest extends BidRequest, DepartureData {}

export interface RoomChangeRequest extends BidRequest, RoomChangeData {}
