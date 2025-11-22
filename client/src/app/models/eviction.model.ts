import {User} from './user/user.model';

export interface Eviction {
  resident: User;
  reason: EvictionReason;
}

export enum EvictionReason {
  NON_PAYMENT = 'NON_PAYMENT',
  NON_RESIDENCE = 'NON_RESIDENCE',
  RULE_VIOLATION = 'RULE_VIOLATION',
}

export const EVICTION_REASON_LOCALIZATION_MAP = {
  [EvictionReason.NON_PAYMENT]: 'Просрочка платежа',
  [EvictionReason.NON_RESIDENCE]: 'Самовольное отсутствие',
  [EvictionReason.RULE_VIOLATION]: 'Нарушение правил'
};
