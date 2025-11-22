import {Component, EventEmitter, Input, Output} from '@angular/core';
import {User} from '../../../../models/user/user.model';
import {Card} from 'primeng/card';
import {NgIf} from '@angular/common';
import {RouterLink} from '@angular/router';
import {BID_STATUS_COLOR_MAP, BID_TYPE_MAP, BidStatus, BidType} from '../../../../models/bid/bid.model';
import {Utils} from '../../../../services/utils';
import {Role} from '../../../../models/auth/role.model';

@Component({
  selector: 'bid-card',
  standalone: true,
  templateUrl: './bid-card.component.html',
  imports: [
    Card,
    NgIf,
    RouterLink
  ]
})
export class BidCardComponent {
  @Input() number!: number;
  @Input() sender?: User;
  @Input() manager?: User;
  @Input() type!: BidType;
  @Input() status!: BidStatus;

  @Output() open = new EventEmitter<number>();

  protected readonly Utils = Utils;
  protected readonly Role = Role;
  protected readonly BID_TYPE_MAP = BID_TYPE_MAP;
  protected readonly BID_STATUS_COLOR_MAP = BID_STATUS_COLOR_MAP;
}
